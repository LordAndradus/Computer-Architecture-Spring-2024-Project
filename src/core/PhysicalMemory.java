package core;

import core.utils.AddressSplit;
import core.utils.Calculator;

import java.util.HashMap;
import java.util.Objects;

public class PhysicalMemory
{
    private long size;
    private int numPages;
    private int numSysPages;
    private int pageTableBits;
    private int pageTableRAM;
    private double percentMemoryUsed;
    private double percentMemoryUnused;
    private int numProcesses;

    private HashMap<Integer, Integer> data;

    public PhysicalMemory(long physicalMemorySize, double usedPhysicalMemory)
    {
        this.size = physicalMemorySize;
        data = new HashMap<>();

        numPages = (int) (size / 4096);
        pageTableBits = Calculator.getBasePower(numPages, 2) + 1;
        this.percentMemoryUsed = usedPhysicalMemory;
        this.percentMemoryUnused = 100d - (usedPhysicalMemory);
        this.numSysPages = (int) ((double) numPages * ((double) percentMemoryUnused / 100d));
    }

    public int read(AddressSplit address)
    {
        if(Objects.isNull(data.get(address.getAddress()))) data.put(address.getAddress(), Calculator.getRandomInt(0, 256));
        return data.get(address.getAddress());
    }

    public void write(AddressSplit address, int data)
    {
        this.data.put(address.getAddress(), data);
    }

    public long getSize()
    {
        return size;
    }

    public int getNumberOfPhysicalPages()
    {
        return numPages;
    }

    public int getNumberOfSystemPhysicalPages()
    {
        return numSysPages;
    }

    public int getPageTableBits()
    {
        return pageTableBits;
    }

    public int getPageTableRAM()
    {
        return pageTableRAM;
    }

    public double getPercentMemoryUsed()
    {
        return percentMemoryUsed;
    }

    public double getPercentMemoryUnused()
    {
        return percentMemoryUnused;
    }

    public void setPageTableRAM(int size)
    {
        pageTableRAM = (((pageTableBits * numPages * size) / 8) * 2);
    }
}
