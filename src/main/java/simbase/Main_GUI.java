package simbase;

import interfaces.MarketTabPanels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import marketbase.ChartAnalyzer_Main;
import marketbase.Marketplace_Main;
import marketbase.Marketplace_Master;
import marketbase.SimulationAnalyzer_Main;


public class Main_GUI extends JFrame{
	
	JPanel mainPanel = new JPanel(new GridLayout(1,1));
	public JPanel panels[] = {new Marketplace_Main(this), new SimulationAnalyzer_Main(), new ChartAnalyzer_Main()};
	String title[] = {"Marketplace Setup","Simulation Analyzer", "Chart Analyzer"};
	
	public Main_GUI(String title)
	{
		super(title);
		createTabs();
		this.add(mainPanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
		this.setPanelsSize(600,700);
		//this.pack();
	}
	public void createTabs()
	{
        JTabbedPane tabbedPane = new JTabbedPane();    
        
        for(int i = 0;i < panels.length;i++)
        {
        	tabbedPane.addTab(title[i],panels[i]);
        }
        
        mainPanel.add(tabbedPane);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	public void changeTab(int index)
	{
		JTabbedPane tabbedPane = null;
		for(int i = 0;i < mainPanel.getComponentCount();i++)
			if(mainPanel.getComponent(i) instanceof JTabbedPane)
			{
				tabbedPane = (JTabbedPane)mainPanel.getComponent(i);
				break;
			}
		tabbedPane.setSelectedIndex(index%tabbedPane.getComponentCount());		
	}
	
	public int getTab()
	{
		JTabbedPane tabbedPane = null;
		for(int i = 0;i < mainPanel.getComponentCount();i++)
			if(mainPanel.getComponent(i) instanceof JTabbedPane)
			{
				tabbedPane = (JTabbedPane)mainPanel.getComponent(i);
				break;
			}
		return tabbedPane.getSelectedIndex();	
	}
	
	public void setPanelsSize(int width,int height)
	{
		this.setSize(width,height);
		for(int i = 0;i < panels.length; i++)
		{
			if(panels[i] instanceof MarketTabPanels)
				((MarketTabPanels) panels[i]).setPanelSize(width,height);
			else
				panels[i].setSize(width, height);
		}
		
	}
	
	public JPanel getPanels(int index)
	{
		return panels[index];
	}
	
	public int getPanelCount()
	{
		return panels.length;
	}
	
	//Main GUI
	public static void main(String[] args)
	{
		Main_GUI gui = new Main_GUI("Trust Robustness System");
	}

}
