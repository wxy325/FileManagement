
public class FCB extends FileData
{
	
	int m_iBeginIndex = -1;
	
	public FCB(String name)
	{
		super(name);
	}
	
	//文件内容操作
	public void saveFile(String strContent) throws DiskFullExcption
	{
		if (strContent.length() == 0)
		{
			this.deleteFileContent();
		}
		else
		{
			m_iBeginIndex = PhysicDisk.sharePhysicDist().saveFile(strContent,m_iBeginIndex);
			dataChange();
		}
	}
	
	public String getFileContent()
	{
		return PhysicDisk.sharePhysicDist().getFileContent(m_iBeginIndex);
	}
	
	public void deleteFileContent()
	{
		if (m_iBeginIndex != -1) 
		{
			PhysicDisk.sharePhysicDist().deleteFile(m_iBeginIndex);
			m_iBeginIndex = -1;
		}
		dataChange();
	}
	
	public void deleteSelf()
	{
		deleteFileContent();
		dataChange();
	}
	
	//用于在属性窗口显示的信息
	public int getBlockSize()
	{
//		return 0;
		return PhysicDisk.sharePhysicDist().getBlockSize(m_iBeginIndex);
	}
	public String getBlockDescription()
	{
//		return "";
		return PhysicDisk.sharePhysicDist().getBlockDescription(m_iBeginIndex);
	}
	public int getContentLength()
	{
		return getFileContent().length();
	}
}
