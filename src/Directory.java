import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;


public class Directory extends FileData implements Observer
{
	private Map<String, FileData> m_Content; //put remove get
	
	public Collection<FileData> getAllDatas(){return m_Content.values(); };
	
	public void createDirectory(String name) throws DuplicationNameException
	{
		if (m_Content.get(name) == null)
		{
			Directory newDirectory = new Directory(name);
			newDirectory.setPreviousDirectory(this);
			m_Content.put(name, newDirectory);
			newDirectory.addObserver(this);
			dataChange();
		}
		else
		{
			throw new DuplicationNameException();
		}	
	}
	
	public void createFCB(String name) throws DuplicationNameException
	{
		if (m_Content.get(name) == null)
		{
			FCB newFCB = new FCB(name);
			newFCB.setPreviousDirectory(this);
			m_Content.put(name, newFCB);
			newFCB.addObserver(this);
			dataChange();
		}
		else
		{
			throw new DuplicationNameException();
		}
	}
	
	public void delete(FileData data)
	{
		data.deleteObserver(this);
		data.deleteSelf();
		data.deleteObservers();
		m_Content.remove(data.getName());
		dataChange();
	}
	
	public Boolean rename(FileData data,String newName)
	{
		if (m_Content.get(newName) == null)
		{
			String oldName = data.getName();
			data.setName(newName);
			m_Content.put(newName,data);
			m_Content.remove(oldName);
			dataChange();
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public Directory(String name)
	{
		super(name);
		
		m_Content = new HashMap<String,FileData>();	
	}
	
	public int getSize()
	{
		return m_Content.size();
	}
	
	public void deleteSelf()
	{
		Iterator<FileData> iter = getAllDatas().iterator();
		while (iter.hasNext())
		{
			FileData data = iter.next();
			data.deleteSelf();
		}
		m_Content.clear();
		dataChange();
	}
	
	public FileData getDataByName(String name)
	{
		return m_Content.get(name);
	}
	
	public int getContentFileSize()
	{
		int iSize = 0;
		Iterator<FileData> iter = m_Content.values().iterator();
		while(iter.hasNext())
		{
			FileData data = iter.next();
			if (data instanceof Directory)
			{
				Directory directory = (Directory) data;
				iSize = iSize + directory.getContentFileSize();
			}
			else
			{
				iSize++;
			}
		}
		return iSize;
	}
	
	public int getBlockSize()
	{
		int iSize = 0;
		Iterator<FileData> iter = m_Content.values().iterator();
		while(iter.hasNext())
		{
			FileData data = iter.next();
			iSize = iSize + data.getBlockSize();
		}
		return iSize;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		//文件夹内的文件发生变化时收到通知，仅用于通知该文件夹的观察者
		dataChange();
	}
}
