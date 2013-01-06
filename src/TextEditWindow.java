import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.Border;

public class TextEditWindow extends JFrame
{
	private static List<FCB> s_OpenFile = new LinkedList<FCB>();
	private static Map<FCB, TextEditWindow> s_FcbToWindow = new HashMap<FCB,TextEditWindow>();
	public static void openFile(FCB fcb)
	{
		if (s_OpenFile.contains(fcb))
		{
			TextEditWindow window = s_FcbToWindow.get(fcb);
			setWindowTop(window);
		}
		else
		{
			s_OpenFile.add(fcb);
			TextEditWindow newWindow = new TextEditWindow(fcb);
			s_FcbToWindow.put(fcb, newWindow);
		}
	}
	public static boolean haveOpenFile()
	{
		return !s_OpenFile.isEmpty();
	}
	
	private static void setWindowTop(TextEditWindow w)
	{
		w.setAlwaysOnTop(true);
		w.setAlwaysOnTop(false);
	}
	public static void closeTextEditWindow(FCB data)
	{
		if (s_OpenFile.contains(data))
		{
			TextEditWindow window = s_FcbToWindow.get(data);
			window.exit();
		}
	}
	public static void closeAllWindow()
	{
		while (!s_OpenFile.isEmpty())
		{
			s_FcbToWindow.get(s_OpenFile.get(0)).exit();
		}
	}
	
	private FCB m_fcb;
	public FCB getFCB(){return m_fcb;}
	private JTextArea m_ContentField;
	private String m_PreviousString;
	
	
	private TextEditWindow(FCB fcb)
	{
		super(fcb.getName());
		m_fcb = fcb;
		
		setSize(600, 450);
		setVisible(true);
		
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() 
		{
			public void windowClosing(WindowEvent e)
			{				
				beforeExit();
			}
		});
		
		setLayout(new BorderLayout());
		m_ContentField = new JTextArea();
		m_PreviousString = m_fcb.getFileContent();
		m_ContentField.setText(m_PreviousString);
//		m_ContentField.setText(m_fcb.getFileContent());
		m_ContentField.setLineWrap(true);
		m_ContentField.setWrapStyleWord(true);
		add(new JScrollPane(m_ContentField),BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		add(buttonPanel,BorderLayout.NORTH);
		buttonPanel.setLayout(new FlowLayout());
		
		JButton saveButton = new JButton("保存");
		saveButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				save();
			}
		});
		buttonPanel.add(saveButton);

		JButton exitButton = new JButton("退出");
		exitButton.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				beforeExit();
			}
		});
		buttonPanel.add(exitButton);

		this.repaint();
		this.validate();
	}
	
	private void save()
	{
		try 
		{
			m_fcb.saveFile(m_ContentField.getText());
			m_PreviousString = m_ContentField.getText();
		}
		catch (DiskFullExcption e)
		{
			JOptionPane.showMessageDialog(null, "磁盘空间已满，保存失败");
		}
		
	}
	
	private void beforeExit()
	{
		if (!m_PreviousString.equals(m_ContentField.getText())) 
		{
			int selectIndex = JOptionPane.showConfirmDialog(null, "文本内容有改动，是否在退出前进行保存?");
			if(selectIndex == JOptionPane.OK_OPTION)
			{
				try 
				{
					m_fcb.saveFile(m_ContentField.getText());
					TextEditWindow.this.exit();
				} 
				catch (DiskFullExcption e1) 
				{
					JOptionPane.showMessageDialog(null, "磁盘已满，请删除部分内容后再进行保存");
				}
			}
			else if (selectIndex == JOptionPane.NO_OPTION)
			{
				TextEditWindow.this.exit();
			}
		}
		else
		{
			TextEditWindow.this.exit();
		}
	}
	
	private void exit()
	{
		s_FcbToWindow.remove(this.getFCB());
		s_OpenFile.remove(this.getFCB());
		this.setVisible(false);
	}
}
