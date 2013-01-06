import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

public class MainWindow extends JFrame implements Observer
{
	static private final String OUTPUT_FILE_NAME = "data.dat";
	
	private Directory m_RootDirectory;		//整个文件系统的根目录
	private Directory m_CurrentDirectory;	//当前目录	
//	LinkedList<Directory> m_PreviousDirectory;	//当前目录的上级目录
	
	private JPanel m_ContentPanel;
	private JTextField m_AddressField;
	
	private List<DataIcon> m_Icons;
	private DataIcon m_FocusedIcon;
	
	public MainWindow() throws FileNotFoundException, IOException, ClassNotFoundException 
	{
		super("FileManagement By 1152822_wuxiangyu");
		
		m_Icons = new LinkedList<DataIcon>();
		m_FocusedIcon = null;
		
		m_CurrentDirectory =PhysicDisk.sharePhysicDist().getRootDirectory();
		m_CurrentDirectory.addObserver(this);
		
		//界面初始化
		
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);


		this.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e)
			{				
				if (!TextEditWindow.haveOpenFile() 
						|| (JOptionPane.showConfirmDialog
								(null, "还有编辑中的文件还未退出，如果直接退出文件管理系统则未保存的内容将会丢失，是否确定退出？")
								== JOptionPane.OK_OPTION))
				{
					try 
					{
						PhysicDisk.saveStoreData();
					} 
					catch (Exception e1) 
					{
						e1.printStackTrace();
					}
					System.exit(0);
				}
			}
		});
		
		//图标区
		
		this.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane();
		m_ContentPanel = new JPanel();
		m_ContentPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 3, 5));
		m_ContentPanel.setBackground(Color.WHITE);
		scrollPane.setBorder(new TitledBorder(""));
		scrollPane.setViewportView(m_ContentPanel);
		this.add(scrollPane,BorderLayout.CENTER);
		
		
		//地址栏、按钮区
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		this.add(topPanel,BorderLayout.NORTH);
		topPanel.setVisible(true);
		topPanel.setBorder(new TitledBorder(""));
		
		JButton backButton = new JButton("返回上级");
		topPanel.add(backButton,BorderLayout.WEST);
		//返回上级按钮
		backButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				if (m_CurrentDirectory != PhysicDisk.sharePhysicDist().getRootDirectory())
				{
					m_CurrentDirectory.deleteObserver(MainWindow.this);
					m_CurrentDirectory = m_CurrentDirectory.getPreviousDirectory();
					m_CurrentDirectory.addObserver(MainWindow.this);
					refreshIcon();
				}
				
			}
		});
		
		m_AddressField = new JTextField();
		m_AddressField.setVisible(true);
		m_AddressField.setText("/");
		topPanel.add(m_AddressField,BorderLayout.CENTER);
//		m_AddressField.setEditable(false);
		
		JButton gotoButton = new JButton("转到");
		topPanel.add(gotoButton,BorderLayout.EAST);
		gotoButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				MainWindow.this.gotoAddress(m_AddressField.getText());
			}
		});
		//此处设置图标区大小，并添加图标
		m_ContentPanel.setPreferredSize(new Dimension(750, (m_CurrentDirectory.getSize() / 7 + 1) * 105));
		refreshIcon();
		m_ContentPanel.addMouseListener(new PanelClick());
				
		
		this.setSize(800,600);
		this.setResizable(false);
		
		refresh();
		
		
	}
	
	private void gotoAddress(String address)
	{
		String[] splitStrings = address.split("/");
		Directory directory = PhysicDisk.sharePhysicDist().getRootDirectory();
		
		for (int i = 0; i < splitStrings.length - 1; i++)
		{
			if (splitStrings[i].equals(""))
			{
				continue;
			}
			FileData data = directory.getDataByName(splitStrings[i]);
			if ((data instanceof Directory) && (data != null))
			{
				directory = (Directory) data;
			}
			else
			{
				JOptionPane.showMessageDialog(null, "地址错误，请重新输入");
				return;
			}
		}
		if (splitStrings[splitStrings.length - 1].equals(""))
		{
			openData(directory);
		}
		else
		{
			FileData data = directory.getDataByName(splitStrings[splitStrings.length - 1]);
			if (data != null)
			{		
				openData(data);
			}
			else
			{
				JOptionPane.showMessageDialog(null, "地址错误，请重新输入");
			}
		}
	}
	
	private void refreshIcon()
	{
		m_ContentPanel.removeAll();
		while (!m_Icons.isEmpty()) 
		{
			m_Icons.remove(0);
		}
		
		Iterator<FileData> it = m_CurrentDirectory.getAllDatas().iterator();
		while (it.hasNext()) 
		{
			FileData data = (FileData) it.next();
			
			DataIcon icon = new DataIcon(data);
			m_Icons.add(icon);
			m_ContentPanel.add(icon);
			icon.addMouseListener(new DataIconClick());
		}

		m_AddressField.setText(m_CurrentDirectory.getPath() + "/");		
		refresh();
	}

	private void refresh()
	{
		this.repaint();
		this.validate();
	}
	
	public void createDirectory()
	{
		String name = JOptionPane.showInputDialog("请输入名称");
		if (name.length() == 0)
		{
			JOptionPane.showMessageDialog(null, "名称不能为空");
		}
		else
		{
			try
			{
				m_CurrentDirectory.createDirectory(name);
			}
			catch (DuplicationNameException e)
			{
				JOptionPane.showMessageDialog(null, "该名称已存在");
			}
		}

	}
	public void createFCB()
	{
		String name = JOptionPane.showInputDialog("请输入名称");
		if (name.length() == 0)
		{
			JOptionPane.showMessageDialog(null, "名称不能为空");
		}
		else
		{
			try
			{
				m_CurrentDirectory.createFCB(name);
			}
			catch (DuplicationNameException e)
			{
				JOptionPane.showMessageDialog(null, "该名称已存在");
			}
		}
	}
	
	public void deleteData(FileData data)
	{
		if (JOptionPane.showConfirmDialog(null, "确定要删除" + data.getName() + "?") == JOptionPane.OK_OPTION)
		{
			PropertyWindow.closePropertyWindow(data);
			if (data instanceof FCB)
			{
				FCB fcb = (FCB) data;
				TextEditWindow.closeTextEditWindow(fcb);
			}
			m_CurrentDirectory.delete(data);
		}
	}
	
	public void renew()
	{
		if (JOptionPane.showConfirmDialog(null, "确定要格式化?") == JOptionPane.OK_OPTION)
		{
			TextEditWindow.closeAllWindow();
			PropertyWindow.closeAllWindow();
			PhysicDisk.sharePhysicDist().getRootDirectory().deleteSelf();
		}
	}
	
	private void saveData() throws FileNotFoundException, IOException
	{
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(OUTPUT_FILE_NAME));
		output.writeObject(m_RootDirectory);
		output.close();
	}
	
	private void deselectAllIcon()
	{
		Iterator<DataIcon> iter = m_Icons.iterator();
		while (iter.hasNext()) 
		{
			DataIcon icon = (DataIcon) iter.next();
			icon.setSelected(false);
		}
		refresh();
	}
	
	
	private void openData(FileData data)
	{
		if (data instanceof FCB)
		{
			FCB text = (FCB)data;
			TextEditWindow.openFile(text);
		}
		else if (data instanceof Directory)
		{
			m_CurrentDirectory.deleteObserver(this);
			Directory directory = (Directory)data;
			m_CurrentDirectory = directory;
			m_CurrentDirectory.addObserver(this);
			refreshIcon();
		}
		
	}
	private void openPropertyWindow(FileData data)
	{
		PropertyWindow.openPropertyWindow(data);
	}
	
	@Override
	
	public void update(Observable o, Object arg) 
	{
		refreshIcon();
	}
	
	
	//内部类
	
	private class DataIconClick extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				if (e.getClickCount() == 1) 
				{
					//左键单击
					DataIcon icon = (DataIcon)e.getSource();
					if (!icon.getSelected()) 
					{
						MainWindow.this.deselectAllIcon();
						icon.setSelected(true);
						m_FocusedIcon = icon;
					}
				}
				else if (e.getClickCount() == 2)
				{
					//左键双击
					DataIcon icon = (DataIcon)e.getSource();
					MainWindow.this.openData(icon.getTargetData());
				}
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
				//右键单击
				DataIcon icon = (DataIcon)e.getSource();
				MainWindow.this.deselectAllIcon();
				icon.setSelected(true);
				m_FocusedIcon = icon;
				JPopupMenu menu = new IconRightButtonMenu(icon.getTargetData());
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}	
	}
	
	//按钮右键菜单
	private class IconRightButtonMenu extends JPopupMenu
	{
		private FileData m_Target;
		public IconRightButtonMenu(FileData target)
		{
			this();
			m_Target = target;
		}
		
		public IconRightButtonMenu()
		{
			super();
			
			JMenuItem open = new JMenuItem("打开");
			open.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					MainWindow.this.openData(m_Target);
				}
			});
			this.add(open);
			
			JMenuItem delete = new JMenuItem("删除");
			delete.addActionListener(new ActionListener() 
			{
				public void actionPerformed(ActionEvent e) 
				{
					MainWindow.this.deleteData(m_Target);
				}
			});
			this.add(delete);
			
			JMenuItem property = new JMenuItem("属性");
			property.addActionListener(new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					MainWindow.this.openPropertyWindow(m_Target);
				}
			});
			this.add(property);
		}
	}
	
	//监听点击panel,取消所有选择的icon,并触发右键菜单
	private class PanelClick extends MouseAdapter
	{
		public void mouseClicked(MouseEvent e)
		{
			MainWindow.this.deselectAllIcon();
			if (e.getButton() == MouseEvent.BUTTON1)
			{
				//左键
			}
			else if (e.getButton() == MouseEvent.BUTTON3)
			{
				//右键
				JPopupMenu menu = new PanelRightButtonMenu();
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	//panel右键菜单
	private class PanelRightButtonMenu extends JPopupMenu
	{
		public PanelRightButtonMenu()
		{
			super();
			
			JMenuItem newDictory = new JMenuItem("新建文件夹");
			newDictory.addActionListener(new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					MainWindow.this.createDirectory();
				}
			});
			this.add(newDictory);
			
			JMenuItem newFCB = new JMenuItem("新建文件");
			newFCB.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					MainWindow.this.createFCB();
				}
			});
			this.add(newFCB);
			
			if (m_CurrentDirectory == PhysicDisk.sharePhysicDist().getRootDirectory())
			{
				JMenuItem renew = new JMenuItem("格式化");
				renew.addActionListener(new ActionListener() 
				{
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						MainWindow.this.renew();
					}
				});
				this.add(renew);
			}
			
			JMenuItem property = new JMenuItem("属性");
			property.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					MainWindow.this.openPropertyWindow(m_CurrentDirectory);
				}
			});
			this.add(property);
		}
	}
	
}
