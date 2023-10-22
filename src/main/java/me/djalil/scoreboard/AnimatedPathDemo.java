package me.djalil.scoreboard;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * Experimenting.
 */
@SuppressWarnings("serial")
public class AnimatedPathDemo {

	public static void main(String[] args) {
        JFrame win = new JFrame("Superset");
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container cp = win.getContentPane();
        cp.add(new JComponent() {
        	
        	private float ratio = 0;
        	private Timer timer = null;
        	
        	public void setProgress(float ratio) {
        		if (ratio > 1) {
        			ratio = 1;
        		}
        		this.ratio = ratio;
        		this.repaint();
        	}

        	@Override
            public void paintComponent(Graphics g) {
            	Shape shape = new Rectangle2D.Float(250.0f / 2, 150f / 2, 150, 150);
                double len = ((Rectangle2D)shape).getWidth() * 2 + ((Rectangle2D)shape).getHeight() * 2;

                Graphics2D g2 = (Graphics2D) g;
            	
                g2.setColor(Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(5));
                g2.draw(shape);
                
                float dashPhase = (1 - ratio) * (float)len;
                System.out.println("phase " + dashPhase);
                System.out.println("ratio " + ratio);
                float dash[] = {(float)len};
                BasicStroke dashedStroke = new BasicStroke(
                        5f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_MITER,
                        5f, //miter limit
                        dash,
                        dashPhase
                        );

                g2.setColor(Color.MAGENTA.darker());
                g2.setStroke(dashedStroke);
                g2.draw(shape);
                
                if (timer == null) {
                    timer = new Timer(10, new ActionListener() {

    					@Override
    					public void actionPerformed(ActionEvent e) {
    						setProgress(ratio + 0.01f);
    						if (ratio == 1.0f) {
    							timer.stop();
    						}
    					}});
                    this.addMouseListener(new MouseAdapter() {
                    	@Override
                    	public void mouseClicked(MouseEvent e) {
                    		setProgress(0);
                    		timer.start();
                    	}
                    });
                    //timer.start();
                	
                }

            }
        });
        cp.setBackground(Color.WHITE);
        win.setSize(500, 500);
        win.setVisible(true);
    }
}
