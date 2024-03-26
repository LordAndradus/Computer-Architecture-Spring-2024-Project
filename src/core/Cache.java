package core;

import core.utils.AddressSplit;
import core.utils.Calculator;
import data.Block;
import data.Policy;
import data.Set;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class Cache
{
    //Standard parameters
    private final int size;
    private final int blockSize;
    private final int associativity;
    private final int numBlocks;
    private final int numSets;
    private final int overHeadSize;
    private final long implementationSize;
    private final double implementationSizeKB;
    private int instructionPerTimeSlice;

    //Cache memory
    Set[] sets;

    //CPU
    CPU reference;

    //Bitties
    private final int tagBits;
    private final int indexBits;
    private final int blockOffsetBits;

    public Cache(CPU cpu, int size, int blockSize, int associativity, int instructionPerTimeSlice)
    {
        reference = cpu;
        long sizeBytes = Calculator.getBytes(size + "KB");
        this.size = size;
        this.blockSize = blockSize;
        this.associativity = associativity;
        this.instructionPerTimeSlice = instructionPerTimeSlice;

        numBlocks = (int) sizeBytes / blockSize;
        numSets = numBlocks / associativity;
        sets = new Set[numSets];

        //Block offset bits = log_2(blockSize)
        //Index bits = log_2(size) - log_2(blockSize * associativity) -> Alternative: log_2(Cache Size / Block Size / Associativity)
        //Tag bits = 32 - index - offset
        blockOffsetBits = Calculator.getBasePower(blockSize, 2);
        indexBits = Calculator.getBasePower(sizeBytes, 2) - Calculator.getBasePower(blockSize * associativity, 2);
        tagBits = 32 - indexBits - blockOffsetBits;

        implementationSize = (long) (sizeBytes + Math.pow(2, tagBits) + numBlocks);
        implementationSizeKB = implementationSize /1024d;

        overHeadSize = (int) (implementationSize - sizeBytes);

        for(int i = 0; i < sets.length; i++) sets[i] = new Set(associativity, blockSize);
    }

    public boolean addressInCache(AddressSplit address)
    {
        return Objects.isNull(sets[address.getIndex()].getValidBlock(address));
    }

    public boolean checkAddress(AddressSplit address)
    {
        if(addressInCache(address))
        {
            Set set = sets[address.getIndex()];
            Block removable = reference.getReplacementBlock(address);

            if(Objects.isNull(removable))
            {
                System.err.println("ERROR::Unaccounted error - Block should not be null.");
                System.exit(0);
            }

            //Write back to physical memory
            if(removable.isDirty())
            {
                int[] removedData = removable.getData();
            }

            return false;
        }

        return true;
    }

    public boolean read(int addr)
    {
        AddressSplit address = new AddressSplit(addr, indexBits, blockOffsetBits);

        if(addressInCache(address))
        {

        }

        return true;
    }

    public int getSize()
    {
        return size;
    }

    public int getBlockSize()
    {
        return blockSize;
    }

    public int getAssociativity()
    {
        return associativity;
    }

    public int getInstructionPerTimeSlice()
    {
        return instructionPerTimeSlice;
    }

    public int getNumBlocks()
    {
        return numBlocks;
    }

    public int getNumSets()
    {
        return numSets;
    }

    public int getOverHeadSize()
    {
        return overHeadSize;
    }

    public long getImplementationSize()
    {
        return implementationSize;
    }

    public double getImplementationSizeKB()
    {
        return implementationSizeKB;
    }

    public Set[] getSets()
    {
        return sets;
    }

    public int getNumTagBits()
    {
        return tagBits;
    }

    public int getNumIndexBits()
    {
        return indexBits;
    }

    public int getNumBlockOffsetBits()
    {
        return blockOffsetBits;
    }
}
