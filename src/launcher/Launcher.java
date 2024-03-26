package launcher;

import core.CPU;
import core.utils.CommandLineParser;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

public class Launcher
{
    private static int milestone = 1;

    public static void main(String[] args)
    {
        /*
         * Assumptions
         * 32-bit address bus
         * Write-through policy = Write to cache and then to Main Memory
         * ALL data access is 4 bytes (32Bits)
         *
         */

        CPU cpu = CommandLineParser.parseCommands(args);

        switch(milestone)
        {
            case 1 -> {milestone1(cpu);}
            case 2 -> {milestone2(cpu);}
        }
    }

    public static void milestone2(CPU cpu)
    {
        ArrayList<String> output = new ArrayList<>();
        output.add(cpu.information());
    }

    public static void milestone1(CPU cpu)
    {
        ArrayList<String> output = new ArrayList<>();
        output.add(cpu.information());
        output.add(cpu.milestone1());

        File f = new File("Output/Milestone 1/Team_07_Sim_n_M21.txt");
        f.getParentFile().mkdirs();

        try(BufferedOutputStream boss = new BufferedOutputStream(new FileOutputStream(f)))
        {
            for(String s : output) boss.write(s.getBytes());


            if(Desktop.isDesktopSupported())
            {
                Desktop d = Desktop.getDesktop();
                d.open(f);
            }
            else
            {
                System.out.printf("Please open \"%s\" in \"%s\" to view results\n", f.getName(), f.getParentFile());
            }
        }
        catch(IOException IOe)
        {
            System.err.printf("ERROR::Could not create \"%s\" in directory \"%s\": Please make sure you have appropriate privileges\n", f.getName(), f.getParentFile());
            IOe.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
