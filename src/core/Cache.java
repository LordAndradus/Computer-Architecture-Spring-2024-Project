package core;

import core.utils.AddressSplit;
import core.utils.Calculator;
import data.Block;
import data.Policy;
import data.Set;

import java.awt.*;
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
    private final int instructionPerTimeSlice;

    //Cache memory
    Set[] sets;

    //CPU
    CPU referenceCPU;

    //Bitties
    private final int tagBits;
    private final int indexBits;
    private final int blockOffsetBits;

    public Cache(CPU cpu, int size, int blockSize, int associativity, int instructionPerTimeSlice)
    {
        referenceCPU = cpu;
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

    public boolean addressNotInCache(AddressSplit address)
    {
        return Objects.isNull(sets[address.getIndex()].getValidBlock(address));
    }

    public Block getReplacementBlock(AddressSplit address)
    {
        Set set = getSets()[address.getIndex()];
        Block removable;

        if(getCPU().getReplacementPolicy() == Policy.RoundRobin) removable = set.getFirstInQueue();
        else removable = set.getRandomBlock();

        return removable;
    }

    public void insertAddress(AddressSplit address)
    {
        Set set = sets[address.getIndex()];
        Block removable = getReplacementBlock(address);

        if(Objects.isNull(removable))
        {
            System.err.println("ERROR::Unaccounted error - Block should not be null.");
            System.exit(0);
        }

        //Write back to physical memory
        if(removable.isDirty())
        {
            int[] removedData = removable.getData();
            int removedAddress = (removable.getTag() * sets.length + address.getIndex()) * getBlockSize();
            for(int i = 0; i < blockSize; i++) getCPU().physicalMemoryWrite(new AddressSplit(address, i), removedData[i]);
            getCPU().getStatistics().incConflictMisses();
        }

        int[] data = new int[blockSize];
        for(int i = 0; i < blockSize; i++) data[i] = getCPU().physicalMemoryRead(new AddressSplit(address, i));
        getCPU().getStatistics().incCompulsoryMisses();
        removable.writeThrough(address, data);
    }

    public boolean checkAddress(AddressSplit address)
    {
        if(addressNotInCache(address))
        {
            insertAddress(address);
            return false;
        }
        return true;
    }

    public Set retrieveSet(AddressSplit address)
    {
        getCPU().getStatistics().incHits();
        return sets[address.getIndex()];
    }

    public Set houseKeeping(int addr)
    {
        AddressSplit address = new AddressSplit(addr, indexBits, blockOffsetBits);
        if(checkAddress(address)) return null;

        return retrieveSet(address);
    }

    public boolean read(int addr)
    {
        Set set = houseKeeping(addr);
        if(Objects.isNull(set)) return false;
        set.readByte(new AddressSplit(addr, indexBits, blockOffsetBits));
        return true;
    }

    public boolean write(int addr, int data)
    {
        Set set = houseKeeping(addr);
        if(Objects.isNull(set)) return false;
        set.writeByte(new AddressSplit(addr, indexBits, blockOffsetBits), data);
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

    public CPU getCPU()
    {
        return referenceCPU;
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
