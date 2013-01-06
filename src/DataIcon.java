import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Label;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;



public class DataIcon extends JPanel
{
	
	private FileData m_Data;
	public FileData getTargetData()	{return m_Data;}
	
	private Boolean m_fIsSelected;
	
	private JLabel m_NameLabel;
	private JLabel m_IconLabel;
	
	private static final Color NORMAL_COLOR = Color.WHITE;
	private static final Color SELECTED_COLOR = Color.BLUE; 
	
	public DataIcon(FileData data)
	{
		super();
		setLayout(new BorderLayout());
		setBackground(NORMAL_COLOR);
		setPreferredSize(new Dimension(100,100));
		setVisible(true);
		
		m_Data = data;
		m_fIsSelected = false;
		m_NameLabel = new JLabel(m_Data.getName());
		
		m_IconLabel = new JLabel();
		ImageIcon imageIcon = null;
		
		if (m_Data instanceof Directory)
		{
			//文件夹
			imageIcon = new ImageIcon(getClass().getResource("directoryIcon.png"));
		}
		else 
		{
			//文件
			imageIcon = new ImageIcon(getClass().getResource("fileIcon.png"));
		}
		m_IconLabel.setIcon(imageIcon);
		
		m_IconLabel.setVisible(true);
		m_NameLabel.setVisible(true);
		this.add(m_IconLabel,BorderLayout.CENTER);
		this.add(m_NameLabel,BorderLayout.SOUTH);
	}
	
	public void setSelected(Boolean fSelected)
	{
		
		if (m_fIsSelected != fSelected)
		{
			m_fIsSelected = fSelected;
//			m_NameLabel = new JLabel(m_Data.getName());
			if (fSelected)
			{
				setBackground(SELECTED_COLOR);
			}
			else
			{
				setBackground(NORMAL_COLOR);
			}
		}
	}
	public Boolean getSelected()
	{
		return m_fIsSelected;
	}
	
}

