package akg.view;

import akg.model.canvas.CanvasElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.function.Consumer;

public class ToolBar extends JPanel {

    public JButton chooseFileButton = new JButton("Выбрать файл");

    public JButton chooseColorButton = new JButton("Выбрать цвет объекта");

    public JButton chooseBackgroundColorButton = new JButton("Выбрать цвет фона");

    public JButton chooseCubeMapButton = new JButton("Выбрать cube map");

    public JFileChooser fileChooser = new JFileChooser("..");

    public JFileChooser directoryChooser = new JFileChooser("..");

    public JComboBox<CanvasElement.DrawMode> modes = new JComboBox<>(CanvasElement.DrawMode.values());

    public Consumer<File> fileChooserBack;

    public Consumer<Color> colorChooserBack;

    public Consumer<Color> backgroundColorChooserBack;

    public Consumer<CanvasElement.DrawMode> modesChooserBack;

    public Consumer<File> directoryChooserBack;

    public Color color;

    public ToolBar(Color c){
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        color=c;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(Color.LIGHT_GRAY);

        add(chooseFileButton);
        chooseFileButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    {
                        File selectedFile = fileChooser.getSelectedFile();
                        if(fileChooserBack!=null)
                            fileChooserBack.accept(selectedFile);
                    }
                }
            }
        });

        add(chooseColorButton);
        chooseColorButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color chosenColor = JColorChooser.showDialog(null, "Выберите цвет объекта", color);
                if (chosenColor != null) {
                    color=chosenColor;
                    if(colorChooserBack!=null)
                        colorChooserBack.accept(color);
                }
            }
        });

        add(chooseBackgroundColorButton);
        chooseBackgroundColorButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color chosenColor = JColorChooser.showDialog(null, "Выберите цвет фона", color);
                if (chosenColor != null) {
                    color=chosenColor;
                    if(backgroundColorChooserBack!=null)
                        backgroundColorChooserBack.accept(color);
                }
            }
        });

        add(modes);
        modes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(modesChooserBack!=null)
                    modesChooserBack.accept((CanvasElement.DrawMode)modes.getSelectedItem());
            }
        });

        add(chooseCubeMapButton);
        chooseCubeMapButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnValue = directoryChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    {
                        File selectedFile = directoryChooser.getSelectedFile();
                        if(directoryChooserBack!=null)
                            directoryChooserBack.accept(selectedFile);
                    }
                }
            }
        });
    }

}
