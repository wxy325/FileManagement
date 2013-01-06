import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JLabel;

//属性窗口
public class PropertyWindow extends JFrame implements Observer 
{
	//static method
	private static List<FileData> s_OpenFile = new LinkedList<FileData>();
	private static Map<FileData, PropertyWindow> s_DataToWindow = new HashMap<FileData, PropertyWindow>();
	public static void openPropertyWindow(FileData data)
	{
		if (s_OpenFile.contains(data))
		{
			PropertyWindow window = s_DataToWindow.get(data);
			setWindowOnTop(window);
		}
		else
		{
			s_OpenFile.add(data);
			PropertyWindow window = new PropertyWindow(data);
			data.addObserver(window);
			s_DataToWindow.put(data, window);
		}
	}
	private static void setWindowOnTop(PropertyWindow w)
	{
		w.setAlwaysOnTop(true);
		w.setAlwaysOnTop(false);
	}
	public static void closePropertyWindow(FileData data)		//此方法用于文件删除时关闭已打开的文件属性窗口
	{
		if (s_OpenFile.contains(data))
		{
			PropertyWindow window = s_DataToWindow.get(data);
			window.exit();
		}
	}
	public static void closeAllWindow()
	{
		while (!s_OpenFile.isEmpty())
		{
			s_DataToWindow.get(s_OpenFile.get(0)).exit();
		}
	}
	
	//data member
	private FileData m_Data;
	
	private JLabel m_AlterDate = new JLabel();
	//FCB
	private JLabel m_ContentLength = new JLabel();
	private JLabel m_BlockSize = new JLabel();
	private JLabel m_BlockNumber = new JLabel();
	//Directory
	private JLabel m_FileSize = new JLabel();
	
	//member function
	private PropertyWindow(FileData data)
	{
		super(data.getName() + "属性");
		m_Data = data;
		
		setSize(300,400);
		setVisible(true);
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e)
			{				
				exit();
			}
		});
		
		if (data instanceof FCB)
		{
			setLayout(new GridLayout(7, 2));
			
		}
		else
		{
			setLayout(new GridLayout(6, 2));
		}

		
		add(new JLabel("名称:"));
		add(new JLabel(data.getName()));
		add(new JLabel("类型:"));
		JLabel dataType = new JLabel();
		add(dataType);
		add(new JLabel("创建时间:"));
		add(new JLabel(data.getCreateDate()));
		add(new JLabel("修改时间:"));

		add(m_AlterDate);
		
		if (data instanceof FCB)
		{
			//文件长度，所占块数，所在块号
			dataType.setText("文本文档");
			add(new JLabel("文档长度(字符数):"));
			add(m_ContentLength);
			add(new JLabel("所占块数:"));
			add(m_BlockSize);
			add(new JLabel("所占块号:"));
			add(m_BlockNumber);
		}
		else
		{
			//包含文件数，共占块数
			dataType.setText("文件夹");
			add(new JLabel("包含文件数:"));
			add(m_FileSize);
			add(new JLabel("所占块数:"));
			add(m_BlockSize);
		}
		refresh();
	}
	private void refresh()
	{
		m_AlterDate.setText(m_Data.getAlterDate());
		m_BlockSize.setText("" + m_Data.getBlockSize());
		if (m_Data instanceof FCB)
		{
			FCB fcb = (FCB)m_Data;
			m_ContentLength.setText("" + fcb.getContentLength());
			m_BlockNumber.setText(fcb.getBlockDescription());
		}
		else if (m_Data instanceof Directory)
		{
			Directory directory = (Directory)m_Data;
			m_FileSize.setText("" + directory.getContentFileSize());
		}
	}
	
	private void exit()
	{
		s_OpenFile.remove(m_Data);
		s_DataToWindow.remove(m_Data);
		m_Data.deleteObserver(this);
		this.setVisible(false);
	}
	
	//Observer
	@Override
	public void update(Observable o, Object arg) 
	{
		refresh();
	}
}
