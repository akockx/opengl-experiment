/*
 * Copyright (c) 2018 A.C. Kockx, All Rights Reserved.
 */
package rasterizer.util;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author A.C. Kockx
 */
public final class Utils {
    private static final String ESCAPE_KEY_STROKE_STRING = "ESCAPE";

    private Utils() {
    }

    /**
     * Creates and shows a window that contains the given content.
     *
     * @return the created frame.
     */
    public static JFrame createAndShowFrame(Component content, String title, boolean fullScreen) {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(content, BorderLayout.CENTER);

        //exit when escape key is pressed.
        contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(ESCAPE_KEY_STROKE_STRING), ESCAPE_KEY_STROKE_STRING);
        contentPane.getActionMap().put(ESCAPE_KEY_STROKE_STRING, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        //create frame.
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setContentPane(contentPane);

        //display frame.
        if (fullScreen) {
            //for info about full screen rendering see https://docs.oracle.com/javase/tutorial/extra/fullscreen/index.html
            frame.setUndecorated(true);//otherwise frame has a border.
            frame.setResizable(false);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(frame);
        } else {//if not fullScreen.
            frame.setUndecorated(false);
            frame.pack();
            //center frame on screen.
            frame.setLocationRelativeTo(null);
        }
        frame.setVisible(true);
        return frame;
    }

    public static String read(InputStream inputStream) throws Exception {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = reader.readLine();
            while (line != null) {
                builder.append(line).append("\n");
                line = reader.readLine();
            }
        }

        return builder.toString();
    }
}
