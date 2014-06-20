package com.gearlles.fss.gui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import net.miginfocom.swing.MigLayout;
import java.awt.Color;
import java.awt.SystemColor;

public class MainWindow {

	private JFrame frame;
	private JTextField iterationsTextField;
	private JTextField populationSizeTextField;
	private Canvas canvas;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[fill, grow, 70%][fill, grow, 30%]", "[grow]"));
		
		JPanel canvasPanel = new JPanel();
		frame.getContentPane().add(canvasPanel, "cell 0 0,grow");
		canvasPanel.setLayout(new BorderLayout(0, 0));
		
		canvas = new Canvas();
		canvas.setBackground(SystemColor.activeCaption);
		canvasPanel.add(canvas, BorderLayout.CENTER);
		
		JPanel settingsPanel = new JPanel();
		settingsPanel.setBorder(new TitledBorder(null, "Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frame.getContentPane().add(settingsPanel, "cell 1 0,grow");
		settingsPanel.setLayout(new MigLayout("", "[][grow]", "[][]"));
		
		JLabel iterationsLabel = new JLabel("Iterations");
		settingsPanel.add(iterationsLabel, "cell 0 0,alignx trailing");
		
		iterationsTextField = new JTextField();
		iterationsTextField.setText("1000");
		settingsPanel.add(iterationsTextField, "cell 1 0,growx");
		iterationsTextField.setColumns(10);
		
		JLabel populationSizeLabel = new JLabel("Population");
		settingsPanel.add(populationSizeLabel, "cell 0 1,alignx trailing");
		
		populationSizeTextField = new JTextField();
		populationSizeTextField.setText("20");
		settingsPanel.add(populationSizeTextField, "cell 1 1,growx");
		populationSizeTextField.setColumns(10);
		
	}

	public JTextField getIterationsTextField() {
		return iterationsTextField;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void setPopulationSizeTextField(JTextField populationSizeTextField) {
		this.populationSizeTextField = populationSizeTextField;
	}
}
