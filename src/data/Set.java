package data;

import core.utils.AddressSplit;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Set
{
    private int associativity;
    private int blockSize;
    private int accesses;
    private Block[] blocks;

    public Set(int associativity, int blockSize)
    {
        this.associativity = associativity;
        this.blockSize = blockSize;
        this.accesses = 0;

        blocks = new Block[blockSize];
        for(Block block : blocks) block = new Block();
    }

    public Block getValidBlock(AddressSplit address)
    {
        Block current = null;

        for(Block block : blocks) if(block.isValid() && block.getTag() == address.getTag())
        {
            current = block;
            break;
        }

        if(Objects.isNull(current))
        {
            try
            {
                System.err.printf("CRITICAL FAILURE::Failed to fetch block for 0x%s!\n", address.getAddress());
                throw new Exception("Block was null");
            }
            catch(Exception e)
            {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }

        return current;
    }

    public Block getFirstInQueue()
    {
        //First choice: Pick a block that is not yet valid
        for(Block block : blocks) if(!block.isValid()) return block;

        //Second choice: Pick a block with the lowest queueNumber, as that means it was accessed first
        int min = 0;
        for(int i = 1; i < associativity; i++)
        {
            if(blocks[i].getQueueNumber() >= 1) min = Math.min(min, blocks[i].getQueueNumber());
        }

        return blocks[min];
    }

    public Block getRandomBlock()
    {
        return blocks[ThreadLocalRandom.current().nextInt(0, associativity)];
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Reading and writing
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public int readByte(AddressSplit address)
    {
        Block current = getValidBlock(address);
        return current.read(address, ++accesses);
    }

    public int[] readBytes(AddressSplit address, int bytes)
    {
        int[] data = new int[bytes];

        for(int i = 0; i < bytes; i++)
        {
            AddressSplit currentAddress = new AddressSplit(address, i);

            Block current = getValidBlock(address);

            data[i] = current.read(address, ++accesses);
        }

        return data;
    }

    public void writeByte(AddressSplit address, int data)
    {
        Block current = getValidBlock(address);
        current.fill(address, data, ++accesses);
    }

    public void writeBytes(AddressSplit address, int bytes)
    {
        int[] data = readBytes(address, bytes);

        for(int i = 0; i < bytes; i++)
        {
            Block current = getValidBlock(new AddressSplit(address, i));
            current.fill(address, data[i], ++accesses);
        }
    }
}
