package data;

import core.utils.AddressSplit;

public class Block
{
    boolean valid;
    boolean dirty;
    int tag;
    int blockSize;
    int queueNumber;

    int[] data;

    public Block()
    {
        this(0);
    }

    public Block(int blockSize)
    {
        this.blockSize = blockSize;
        this.data = new int[blockSize];
        queueNumber = 0;
        this.dirty = false;
        this.valid = false;
    }

    public int read(AddressSplit address, int access)
    {
        return data[address.getOffset() % blockSize];
    }

    public void fill(AddressSplit address, int data, int access)
    {
        if(queueNumber == 0) queueNumber = access;
        this.data[address.getOffset() % blockSize] = data;
        setValid(true);
        setDirty(true);
    }

    public void writeThrough(AddressSplit address, int data, int access)
    {

    }

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
    }

    public int getTag()
    {
        return tag;
    }

    public void setTag(int tag)
    {
        this.tag = tag;
    }

    public int[] getData()
    {
        return this.data;
    }

    public int getQueueNumber()
    {
        return queueNumber;
    }

    public String toString()
    {
        return String.format("""
                Tag:        %s
                Queue:      %s
                Valid?      %b
                Dirty?      %b
                """,
                getTag(), getQueueNumber(),
                isValid(), isDirty());
    }
}
