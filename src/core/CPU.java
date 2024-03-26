package core;

import core.utils.AddressSplit;
import core.utils.Calculator;
import data.Block;
import data.Policy;
import data.Set;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CPU
{
    private ArrayList<File> traces = new ArrayList<>();
    private Cache cache;
    private PhysicalMemory physicalMemory;
    private TranslationLookasideBuffer tlb;
    private Policy replacement;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Emulation specifics
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String milestone1()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\n***** Trace file addresses and bytes *****\n");

        for (File trace : traces)
        {
            sb.append(String.format("Trace file: %s\n", trace.getName()));

            try (BufferedReader br = new BufferedReader(new FileReader(trace)))
            {
                String line;
                int addressCounter = 20;

                while (br.ready() && addressCounter > 0)
                {
                    line = br.readLine();
                    if (line.isBlank()) continue;

                    Pattern addressPattern = Pattern.compile("[0-9aA-zZ]{8}");
                    Matcher addressMatcher = addressPattern.matcher(line);

                    String[] elements = Arrays.stream(line.split(" ")).filter(str -> !str.isBlank()).toArray(String[]::new);

                    int bytes = 4;

                    if (Calculator.hashString(elements[0]) == Calculator.hashString("EIP"))
                    {
                        bytes = Integer.parseInt(
                                elements[1].substring(
                                        1, elements[1].indexOf(")")
                                ));
                        sb.append(elements[2]).append(": (").append(String.format("%02d", bytes)).append(")\n");
                        addressCounter--;
                    }
                    else
                    {
                        if (Long.parseLong(elements[1], 16) != 0)
                        {
                            sb.append(elements[1]).append(": (").append(String.format("%02d", bytes)).append(")\n");
                            addressCounter--;
                        }
                        if (Long.parseLong(elements[4], 16) != 0)
                        {
                            sb.append(elements[4]).append(": (").append(String.format("%02d", bytes)).append(")\n");
                            addressCounter--;
                        }
                    }
                }

                sb.append("\n");
            }
            catch (IOException IOe)
            {
                System.err.println("Could not read the trace file \"" + trace.getName() + "\"");
                System.exit(0);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(0);
            }
        }

        return sb.toString();
    }

    public String milestone2()
    {
        for(File trace : traces)
        {
            readTraceFile(trace);
        }

        return null;
    }

    public void readTraceFile(File trace)
    {
        try(BufferedReader br = new BufferedReader(new FileReader(trace)))
        {
            String line = "sample";

            while(br.ready())
            {
                line = br.readLine();
                if(line.isBlank()) continue; //Increment instructions count

                String[] elements = Arrays.stream(line.split(" ")).filter(str -> !str.isBlank()).toArray(String[]::new);

                int bytes = 4;
                boolean hit = false;

                if(Calculator.hashString(elements[0]) == Calculator.hashString(elements[1]))
                {
                    int address = (int) Long.parseLong(elements[2], 16);

                    bytes = Integer.parseInt(
                            elements[1].substring(
                                    1, elements[1].indexOf(")")
                            ));

                    hit = cache.read(address);
                }
            }
        }
        catch (IOException IOe)
        {
            System.err.println("Could not read the trace file \"" + trace.getName() + "\"");
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public Block getReplacementBlock(AddressSplit address)
    {
        Set set = cache.getSets()[address.getIndex()];
        Block removable;

        if(replacement == Policy.RoundRobin) removable = set.getFirstInQueue();
        else removable = set.getRandomBlock();

        return removable;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Setters and getters
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addTraceFile(File trace)
    {
        traces.add(trace);
    }

    public ArrayList<File> getTraceFiles()
    {
        return traces;
    }

    public Cache getCache()
    {
        return cache;
    }

    public void setCache(Cache cache)
    {
        this.cache = cache;
    }

    public PhysicalMemory getPhysicalMemory()
    {
        return physicalMemory;
    }

    public void setPhysicalMemory(PhysicalMemory physicalMemory)
    {
        this.physicalMemory = physicalMemory;
    }

    public TranslationLookasideBuffer getTlb()
    {
        return tlb;
    }

    public void setTlb(TranslationLookasideBuffer tlb)
    {
        this.tlb = tlb;
    }

    public Policy getReplacement()
    {
        return replacement;
    }

    public void setReplacement(Policy replacement)
    {
        this.replacement = replacement;
    }

    public String information()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("Cache Simulator CS 3853 Spring 2024 - Group #07\n\n");

        if(!traces.isEmpty())
        {
            for(File trace : traces) sb.append(String.format("Trace file: %s\n", trace.getName()));
        }

        sb.append(String.format("""
                %s***** Cache Input Parameters *****
                Cache Size:                     %s KB
                Block Size:                     %s bytes
                Associativity:                  %s
                Replacement Policy              %s
                Physical Memory                 %s MB
                Percent Memory Used by System:  %s
                Instructions / Time Slice:      %s
                
                ***** Cache Calculated Values *****
                Total # Blocks:                 %s
                Tag Size:                       %s bits
                Index Size:                     %s bits
                Total # Rows:                   %s
                Overhead Size:                  %s bytes
                Implementation Memory Size:     %s KB (%s bytes)
                Cost:                           $%s @ ($0.15 / KB)
                
                ***** Physical Memory Calculations *****
                Number of Physical Pages:       %s
                Number of Pages for System:     %s
                Size of Page Table Entry:       %s bits
                Total RAM for Page Table(s):    %s bytes
                """, (traces.isEmpty()) ? "" : "\n",
                //First Block
                cache.getSize(), cache.getBlockSize(), cache.getAssociativity(), replacement.toString(),
                physicalMemory.getSize() / (1024 * 1024), physicalMemory.getPercentMemoryUnused(), cache.getInstructionPerTimeSlice(),
                //Second Block
                cache.getNumBlocks(), cache.getNumTagBits(), cache.getNumIndexBits(), cache.getNumSets(),
                cache.getOverHeadSize(), String.format("%.2f", cache.getImplementationSizeKB()),
                cache.getImplementationSize(), String.format("%.2f", cache.getImplementationSizeKB() * 0.15),
                //Final Block
                physicalMemory.getNumberOfPhysicalPages(), physicalMemory.getNumberOfSystemPhysicalPages(),
                physicalMemory.getPageTableBits(), physicalMemory.getPageTableRAM()
                ));

        return sb.toString();
    }
}
