//PhysicDisk.java
//与硬盘存储相关的类

import java.io.*;
import java.util.Observable;
import java.util.concurrent.CountDownLatch;

public class PhysicDisk implements Serializable 
{
	//假设物理硬盘分为100块，其中第1块(index = 0)用于存文件结构、目录结构等信息，后面99块用于存放文件数据，后99块放满则说明硬盘已满
	//
	static private final String OUTPUT_FILE_NAME = "data.dat";
	
	private static final int TOTAL_BLOCK_NUMBER = 100;
	private int m_RestSize = TOTAL_BLOCK_NUMBER - 1;
	private DiskBlock[] m_PhysicDiskBlocks = new DiskBlock[TOTAL_BLOCK_NUMBER];
	private boolean[] m_fFreeBlocks = new boolean[TOTAL_BLOCK_NUMBER];
	
	//单例模式
	private static PhysicDisk s_PhysicDisk = null;
	public static PhysicDisk sharePhysicDist()
	{
		if (s_PhysicDisk == null)
		{
			loadStoreData();		
			if (s_PhysicDisk == null) 
			{
				s_PhysicDisk = new PhysicDisk();	
			}
		}
		return s_PhysicDisk;
	}
	private PhysicDisk()
	{
		for (int i = 1; i < m_fFreeBlocks.length; i++) 
		{
			m_fFreeBlocks[i] = true;
		}
		m_fFreeBlocks[0] = false;
		
		for (int i = 1; i < m_PhysicDiskBlocks.length; i++) 
		{
			m_PhysicDiskBlocks[i] = new TextContentBlock();
		}
		m_PhysicDiskBlocks[0] = new FileSystemBlock(); 
	}
	
	
	//文件系统块操作
	public Directory getRootDirectory()
	{
		FileSystemBlock systemBlock = (FileSystemBlock)m_PhysicDiskBlocks[0];
		return systemBlock.getRootDirectory();
	}
	
	//文件内容块操作
	public String getFileContent(int iBeginIndex)
	{
		String outputString = new String();
		int iNowIndex = iBeginIndex;
		while (iNowIndex != -1)
		{
			TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[iNowIndex];
			if (contentBlock.getNextBlockIndex() != -1)
			{
			outputString = outputString + new String(contentBlock.getContent());
			}
			else
			{
				//java char[]转String似乎不会以0截断，所以需要这样处理；
				int iCount = 0;
				char[] content = contentBlock.getContent();
				while(iCount < content.length && content[iCount] != 0 )
				{
					iCount++;
				}
				outputString = outputString + new String(content, 0, iCount);
			}
			iNowIndex = contentBlock.getNextBlockIndex();
		}
		return outputString;
	}
	
	private static int getStringBlockLength(String strContent)
	{
		int iLength = strContent.length() / DiskBlock.MAX_STRING_DATA_LENGTH;
		if (DiskBlock.MAX_STRING_DATA_LENGTH * iLength < strContent.length())
		{
			++iLength;
		}
		return iLength;
	}
	
	public int saveFile(String strContent,int iBeginIndex) throws DiskFullExcption
	{
		if (iBeginIndex == -1)
		{
			return this.saveFile(strContent);
		}
		
		int iPreviousLength = getFileContentLength(iBeginIndex);
		int iNewLength = getStringBlockLength(strContent);
		if (iPreviousLength + m_RestSize < iNewLength)
		{
			throw new DiskFullExcption();
		}
		
		
		if (iPreviousLength < iNewLength)
		{
			//块数增加
			int index = 0,iNextBlock = iBeginIndex;
			TextContentBlock nowBlock = (TextContentBlock)m_PhysicDiskBlocks[iBeginIndex];
			iNextBlock = nowBlock.getNextBlockIndex();
			while(iNextBlock != -1)
			{
//				TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[iNowBlock];
//				contentBlock.setContent(this.cutStringByIndex(strContent, index));
				nowBlock.setContent(this.cutStringByIndex(strContent, index));
				index++;
				nowBlock = (TextContentBlock)m_PhysicDiskBlocks[iNextBlock];
				iNextBlock = nowBlock.getNextBlockIndex();
			}
			nowBlock.setContent(this.cutStringByIndex(strContent, index));
			index++;
			//调用辅助函数将字符串剩余部分写入
			char[] newString = new char[strContent.length() - index * DiskBlock.MAX_STRING_DATA_LENGTH];
			strContent.getChars(index * DiskBlock.MAX_STRING_DATA_LENGTH, strContent.length(), newString, 0);
			String restString = new String(newString);
			nowBlock.setNextBlockIndex(saveFile(restString));
		}
		else
		{
			//块数减少或不变
			int iNowBlockIndex = iBeginIndex;
			for (int i = 0; i < iNewLength - 1; i++)
			{
				TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[iNowBlockIndex];
				contentBlock.setContent(this.cutStringByIndex(strContent, i));
				iNowBlockIndex = contentBlock.getNextBlockIndex();
			}
			//处理最后一块，写入内容，并删除后序块
			TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[iNowBlockIndex];
			contentBlock.setContent(this.cutStringByIndex(strContent, iNewLength - 1));
			this.deleteFile(contentBlock.getNextBlockIndex());
			contentBlock.setNextBlockIndex(-1);
		}
		return iBeginIndex;
	}
	
	public int saveFile(String strContent) throws DiskFullExcption
	{
		//辅助函数
		//将传入的字符串写入内存空闲部分，并返回起始块号  
		int iLength = getStringBlockLength(strContent);
		
		if (iLength > m_RestSize)
		{
			throw new DiskFullExcption();
		}
		m_RestSize = m_RestSize - iLength;
		int iBeginIndex = -1;
		TextContentBlock previousBlock = null;
		for (int i = 0; i < iLength; i++) 
		{
			int iEmptyBlockIndex = this.getEmptyBlock();
			if (i == 0)
			{
				iBeginIndex = iEmptyBlockIndex;
			}
			else
			{
				previousBlock.setNextBlockIndex(iEmptyBlockIndex);
			}
			m_fFreeBlocks[iEmptyBlockIndex] = false;
			TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[iEmptyBlockIndex];
			char[] strForThisBlock = cutStringByIndex(strContent, i);
			contentBlock.setContent(strForThisBlock);
			if (i == iLength - 1)
			{
				contentBlock.setNextBlockIndex(-1);
			}
			previousBlock = contentBlock;
		}
		return iBeginIndex;
	}
	
	private static char[] cutStringByIndex(String inputString,int index)
	{
		int iLength = getStringBlockLength(inputString);
		
		if (index < iLength - 1)
		{
			char[] newString = new char[DiskBlock.MAX_STRING_DATA_LENGTH];
			inputString.getChars(index * DiskBlock.MAX_STRING_DATA_LENGTH, (index + 1) * DiskBlock.MAX_STRING_DATA_LENGTH, newString, 0);
			return newString;
//			strForThisBlock = new String(strContent.get)
		}
		else if (index == iLength - 1) 
		{
			char[] newString = new char[inputString.length() - index * DiskBlock.MAX_STRING_DATA_LENGTH];
			inputString.getChars(index * DiskBlock.MAX_STRING_DATA_LENGTH, inputString.length(), newString, 0);
			return newString;
		}
		else
		{
			return null;
		}
	}
	
	private int getEmptyBlock()
	{
		for (int i = 0; i < m_fFreeBlocks.length; i++) 
		{
			if (m_fFreeBlocks[i])
			{
				return i;
			}
		}
		return -1;
	}
	
	private int getFileContentLength(int iBeginIndex)
	{
		int i = iBeginIndex;
		int iCount = 0;
		while (i != -1)
		{
			iCount++;
			TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[i];
			i = contentBlock.getNextBlockIndex();
		}
		return iCount;
	}
	
	public void deleteFile(int iBeginIndex)
	{
		int iNowIndex = iBeginIndex;
		while(iNowIndex != -1)
		{
			TextContentBlock contentBlock = (TextContentBlock)m_PhysicDiskBlocks[iNowIndex];
			m_fFreeBlocks[iNowIndex] = true;
			iNowIndex = contentBlock.getNextBlockIndex();
			contentBlock.setContent(null);
			contentBlock.setNextBlockIndex(-1);
			m_RestSize++;
		}
	}
	
	//数据本地存储
	public static void saveStoreData() throws FileNotFoundException, IOException
	{
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILE_NAME));
		output.writeObject(s_PhysicDisk);
		output.close();
	}
	public static void loadStoreData()
	{
		try 
		{
			ObjectInput input = new ObjectInputStream(new FileInputStream(OUTPUT_FILE_NAME));
			s_PhysicDisk = (PhysicDisk)input.readObject();
		}
		catch (Exception e)
		{
			s_PhysicDisk = null;
		}
	}
	
	public int getBlockSize(int beginIndex)
	{
		int nowIndex = beginIndex, iSize = 0;
		while (nowIndex != -1)
		{
			iSize++;
			TextContentBlock block = (TextContentBlock) m_PhysicDiskBlocks[nowIndex];
			nowIndex = block.getNextBlockIndex();		
		}
		return iSize;
	}
	
	public String getBlockDescription(int beginIndex)
	{
		String output = new String();
		int nowIndex = beginIndex;
		while (nowIndex != -1)
		{
			output = output + nowIndex + " ";
			TextContentBlock block = (TextContentBlock) m_PhysicDiskBlocks[nowIndex];
			nowIndex = block.getNextBlockIndex();
		}
		return output;
	}
}


class DiskBlock extends Observable implements Serializable
{
	public static final int MAX_STRING_DATA_LENGTH = 128;
}


//用于存储文件系统，FCB，目录结构等信息
class FileSystemBlock extends DiskBlock
{
	private Directory m_RootDirectory;
	public Directory getRootDirectory(){return m_RootDirectory;}
	public FileSystemBlock() 
	{
		m_RootDirectory = new Directory("");
		m_RootDirectory.setPreviousDirectory(null);
	}
}


//用于储存文件数据，最大长度为MAX_STRING_DATA_LENGTH，超出长度则在设置时返回false,
//并且具有一个指向下一块的index,结尾index = -1;
class TextContentBlock extends DiskBlock
{
	private char[] m_StrData = new char[super.MAX_STRING_DATA_LENGTH];
	private int m_iNextBlockIndex = -1;
	
	public char[] getContent(){return m_StrData;}
	public boolean setContent(char[] strData)
	{
		if (strData == null)
		{
			for (int i = 0; i < m_StrData.length; i++) 
			{
				m_StrData[i] = 0;
			}
			return true;
		}
		else if (strData.length <= super.MAX_STRING_DATA_LENGTH)
		{
			for (int i = 0; i < strData.length; i++)
			{
				m_StrData[i] = strData[i];
			}
			for (int i = strData.length; i < super.MAX_STRING_DATA_LENGTH; i++) 
			{
				m_StrData[i] = 0;
			}
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getNextBlockIndex(){return m_iNextBlockIndex;}
	public void setNextBlockIndex(int index) {m_iNextBlockIndex = index;}
}
