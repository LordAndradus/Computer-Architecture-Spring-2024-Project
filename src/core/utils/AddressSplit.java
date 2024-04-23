package core.utils;

public class AddressSplit
{
    //Bitties
    int indexBits;
    int offsetBits;

    int tag;
    int index;
    int offset;
    int address;

    public AddressSplit(AddressSplit copy, int step)
    {
        this(copy.getAddress() + step, copy.getIndexBits(), copy.getOffsetBits());
    }

    public AddressSplit(int address, int indexBits, int blockOffsetBits)
    {
        this.indexBits = indexBits;
        this.offsetBits = blockOffsetBits;
        this.address = address;
        this.tag = address >>> (indexBits + blockOffsetBits);
        this.index = (address >>> blockOffsetBits) & ((1 << indexBits) - 1);
        this.offset = address & ((1 << blockOffsetBits) - 1);
    }

    public boolean isCopyOf(AddressSplit original)
    {
        return getTag() == original.getTag() && getIndex() == original.getIndex() && getOffset() == original.getOffset();
    }

    public static AddressSplit copyOf(AddressSplit original)
    {
        return new AddressSplit(original.getAddress(), original.getIndexBits(), original.getOffsetBits());
    }

    public int getIndexBits()
    {
        return indexBits;
    }

    public int getOffsetBits()
    {
        return offsetBits;
    }

    public int getTag()
    {
        return tag;
    }

    public int getIndex()
    {
        return index;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getAddress()
    {
        return address;
    }

    public void setAddress(int address)
    {
        this.address = address;
    }

    public String toString()
    {
        return String.format("Address: 0x%08x | Tag: 0x%08x | Index: 0x%08x | Offset: 0x%08x", address, tag, index, offset);
    }
}
