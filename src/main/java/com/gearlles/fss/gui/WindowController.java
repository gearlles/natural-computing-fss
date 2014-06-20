package com.gearlles.fss.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gearlles.fss.core.FSSSearch;
import com.gearlles.fss.core.Fish;

public class WindowController {
	
	Logger logger = LoggerFactory.getLogger(WindowController.class);
	
	private MainWindow mainWindow;
	private BufferedImage frame;
	private FSSSearch search;
	private boolean isRunning = true;
	private int it =0;
	
	/**
	 * Particle's size, in pixels.
	 */
	private final int PARTICLE_SIZE = 7;
	
	/**
	 * How many frames since the last update.
	 */
	public int frames;
	
	/**
	 * The FPS.
	 */
	public int currentFPS;
	
	/**
	 * FPS limit.
	 */
	private final int FPS_LIMIT = 20;
	
	/**
	 * Used to calculate FPS.
	 */
	public long lastTime;
	
	public WindowController() {
		mainWindow = new MainWindow();
		mainWindow.getCanvas().createBufferStrategy(2);
		search = new FSSSearch();
	}
	
	private void render() {
		
		while(isRunning) {
			long renderStart = System.currentTimeMillis();
			updateLogic();
			while ( updateUi(search.getSchool()) );
    		// FPS limiting here
    		long renderTime = (System.currentTimeMillis() - renderStart);
    		try {
    			Thread.sleep(Math.max(0, (1000 / FPS_LIMIT) - renderTime));
    		} catch (InterruptedException e) {
    			Thread.interrupted();
    			break;
    		}
    		
    		calculateFPs();
		}
	}

	private void calculateFPs() {
		if(System.currentTimeMillis() - lastTime >= 1000){
		    currentFPS = frames;
		    frames = 0;
		    lastTime = System.currentTimeMillis();
		}
		frames++;
	}

	private boolean updateUi(List<Fish> school) {
		Canvas canvas = mainWindow.getCanvas();
		BufferStrategy bufferStrategy = canvas.getBufferStrategy();
		frame = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) frame.getGraphics();
	    Graphics2D g = (Graphics2D)bufferStrategy.getDrawGraphics();
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
	    for (int i = 0; i < school.size(); i++) {
	    	BufferedImage image = new BufferedImage(PARTICLE_SIZE, PARTICLE_SIZE, BufferedImage.TYPE_INT_RGB);
	 	    Graphics imageGraphics = image.getGraphics();
	 	    imageGraphics.setColor(Color.YELLOW);
	 	    imageGraphics.fillOval(0, 0, PARTICLE_SIZE, PARTICLE_SIZE);
	 	    
	 	    // TODO get fish position
	 	    double fishX = school.get(i).getPosition()[0] + search.getRANGE();
	 	   double fishY = school.get(i).getPosition()[1] + search.getRANGE();
	 	   
			int x = (int) ((fishX * canvas.getWidth()) / (2 * search.getRANGE()));
			int y = (int) ((fishY * canvas.getHeight()) / (2 * search.getRANGE()));
	 	   	graphics.drawImage(image, x, y, null);
		}
	    
	    graphics.drawString(String.format("FPS: %s", this.currentFPS), 10, 10);
	    g.drawImage(frame, 0, 0, null);
	    g.dispose();
	    
	    bufferStrategy.show();
	    return bufferStrategy.contentsLost();
	}
	
	private void updateLogic() {
			search.iterateOnce(it++);
	}

	public void startSearch(boolean headless) {
		render();
	}
}
