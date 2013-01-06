import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;


abstract public class FileData  extends Observable implements Serializable
{
	private String m_Location;
	public String getLocation(){return m_Location;}
	public void setLocation(String location){m_Location = location;}
	
	private String m_Name;
	public String getName(){return m_Name;}
	public void setName(String name){m_Name = name;}
	
	private Directory m_PreviousDirectory;
	public Directory getPreviousDirectory(){return m_PreviousDirectory;}
	public void setPreviousDirectory(Directory previousDirectory)
	{
		m_PreviousDirectory = previousDirectory;
		if (m_PreviousDirectory == null)
		{
			m_Path = "";
		}
		else
		{
			m_Path = previousDirectory.getPath() + "/" + m_Name;
		}
	}
	
	private String m_Path;
	public String getPath(){return m_Path;}
//	public void setPath(String path){m_Path = path;}
	
	private String m_CreateDate;
	private void setCreateDate(){m_CreateDate = DateAndTimeManage.getCurrentDateAndTime();}
	public String getCreateDate(){return m_CreateDate;}
	
	private String m_AlterDate;
	protected void setAlterDate(){m_AlterDate = DateAndTimeManage.getCurrentDateAndTime();}
	public String getAlterDate(){return m_AlterDate;}
	
	FileData(String name)
	{
		m_PreviousDirectory = null;
		setName(name);
		setCreateDate();
	}

	protected void dataChange()
	{
//		System.out.println("bbb");
		setAlterDate();
		setChanged();
		notifyObservers();
	}
	
	abstract public void deleteSelf();
	abstract public int getBlockSize();
}


class DateAndTimeManage
{
	public static String getCurrentDateAndTime()
	{
		Date now = new Date();
		Calendar cal = Calendar.getInstance(); 
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		return dateFormat.format(now);
	}
}