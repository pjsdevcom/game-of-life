package life;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class GameOfLife extends JFrame {
    private static final int MIN_GAME_SPEED = 0;
    private static final int MAX_GAME_SPEED = 20;
    private static final int MAX_NUM_GENERATIONS = 5000;

    private int fieldSize = 25;
    private Color cellsColour = Color.BLACK;
    private int gameSpeed = 10;
    private Universe universe = new Universe(fieldSize);

    private final JLabel generationLabel;
    private final JLabel aliveLabel;
    private final Field field = new Field();

    private GameThread gameThread;

    public GameOfLife() {
        super("Game of Life");

        generationLabel = new JLabel("Generation #" + universe.getGenerationNumber());
        aliveLabel = new JLabel("Alive: " + universe.getAliveCellsCount());

        initGame();
    }

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(GameOfLife::new);
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.PAGE_AXIS));
        controlsPanel.setMaximumSize(new Dimension(200, 100));
        controlsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel controlButtonsPanel = new JPanel();
        controlButtonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JToggleButton startButton = new JToggleButton("Start");
        startButton.setName("PlayToggleButton");
        startButton.setPreferredSize(new Dimension(85, 30));
        startButton.addActionListener(e -> {
            switch (startButton.getText()) {
                case "Start":
                    gameThread = new GameThread();
                    gameThread.start();
                    startButton.setText("Pause");
                    break;
                case "Pause":
                    gameThread.pause();
                    startButton.setText("Resume");
                    break;
                case "Resume":
                    gameThread.res();
                    startButton.setText("Pause");
                    break;
            }
        });
        JButton resetButton = new JButton("Reset");
        resetButton.setName("ResetButton");
        resetButton.setPreferredSize(new Dimension(85, 30));
        resetButton.addActionListener(e -> {
            if (gameThread != null) {
                gameThread.pause();
            }

            JDialog dialog = new JDialog(this, "Game Reset", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(300, 140);

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridLayout(2, 2, 10, 10));

            JPanel bottomPanel = new JPanel();

            JLabel fieldSizeLabel = new JLabel("Field Size:");
            fieldSizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            Integer[] sizes = Stream.iterate(10, n -> n <= 100, n -> n + 5).toArray(Integer[]::new);
            JComboBox<Integer> fieldSizeSelect = new JComboBox<>(sizes);
            fieldSizeSelect.setSelectedItem(fieldSize);

            JLabel colourLabel = new JLabel("Color of Cells:");
            colourLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            Map<String, Color> colours = new TreeMap<>();
            colours.put("Black", Color.BLACK);
            colours.put("Grey", Color.GRAY);
            colours.put("Red", Color.RED);
            colours.put("Green", Color.GREEN);
            colours.put("Blue", Color.BLUE);
            colours.put("Cyan", Color.CYAN);
            colours.put("Magenta", Color.MAGENTA);
            colours.put("Yellow", Color.YELLOW);
            JComboBox<String> colourSelect = new JComboBox<>(colours.keySet().toArray(String[]::new));
            colourSelect.setSelectedItem(colours.entrySet().stream()
                    .filter(entry -> entry.getValue() == cellsColour)
                    .findFirst().orElse(Map.entry("Black", Color.BLACK)).getKey());

            JButton submitButton = new JButton("OK");
            submitButton.addActionListener(evt -> {
                fieldSize = sizes[fieldSizeSelect.getSelectedIndex()];
                String selectedColour = (String) colourSelect.getSelectedItem();
                cellsColour = colours.get(selectedColour);

                resetGame();
                startButton.setSelected(false);
                startButton.setText("Start");

                dialog.setVisible(false);
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(evt -> {
                if (gameThread != null) {
                    gameThread.res();
                }

                dialog.setVisible(false);
            });

            mainPanel.add(fieldSizeLabel);
            mainPanel.add(fieldSizeSelect);
            mainPanel.add(colourLabel);
            mainPanel.add(colourSelect);

            bottomPanel.add(submitButton);
            bottomPanel.add(cancelButton);

            dialog.add(mainPanel, BorderLayout.CENTER);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        controlButtonsPanel.add(startButton);
        controlButtonsPanel.add(resetButton);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.PAGE_AXIS));

        JLabel sliderLabel = new JLabel("Game Speed", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_GAME_SPEED, MAX_GAME_SPEED, gameSpeed);
        speedSlider.setFont(new Font("Courier", Font.PLAIN, 12));
        speedSlider.setMajorTickSpacing(5);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> gameSpeed = speedSlider.getValue());

        sliderPanel.add(sliderLabel);
        sliderPanel.add(speedSlider);

        controlsPanel.add(controlButtonsPanel);
        controlsPanel.add(sliderPanel);

        leftPanel.add(controlsPanel);

        JPanel labelsPanel = new JPanel();
        labelsPanel.setLayout(new BoxLayout(labelsPanel, BoxLayout.PAGE_AXIS));
        labelsPanel.setPreferredSize(new Dimension(200, 100));
        labelsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Font font = new Font("Courier", Font.BOLD, 18);
        generationLabel.setName("GenerationLabel");
        generationLabel.setFont(font);
        generationLabel.setBorder(BorderFactory.createEmptyBorder(10, 12, 5, 12));
        aliveLabel.setName("AliveLabel");
        aliveLabel.setFont(font);
        aliveLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));

        labelsPanel.add(generationLabel);
        labelsPanel.add(aliveLabel);

        leftPanel.add(labelsPanel);

        add(leftPanel, BorderLayout.WEST);
        add(field, BorderLayout.CENTER);

        setVisible(true);
    }

    private void resetGame() {
        universe = new Universe(fieldSize);
        field.update();

        generationLabel.setText("Generation #" + universe.getGenerationNumber());
        aliveLabel.setText("Alive: " + universe.getAliveCellsCount());
    }

    private class Field extends Canvas {
        @Override
        public void paint(Graphics g) {
            int fieldWidth = getWidth();
            int fieldHeight = getHeight();
            int cellWidth = fieldWidth / fieldSize;
            int cellHeight = fieldHeight / fieldSize;

            for (int i = 0; i <= fieldSize; i++) {
                g.drawLine(i * cellWidth, 0, i * cellWidth, cellHeight * fieldSize);
                g.drawLine(0, i * cellHeight, cellWidth * fieldSize, i * cellHeight);
            }

            boolean[][] generation = universe.getCurrentGeneration();
            for (int i = 0; i < generation.length; i++) {
                for (int j = 0; j < generation[i].length; j++) {
                    if (generation[i][j]) {
                        g.setColor(cellsColour);
                        g.fillRect(cellWidth * i, cellHeight * j, cellWidth, cellHeight);
                    }
                }
            }
        }

        public void update() {
            super.update(getGraphics());
        }
    }

    private class GameThread extends Thread {
        private final Object lock = this;
        private boolean pause = false;

        public void pause() {
            pause = true;
        }
        public void res() {
            pause = false;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
        private void pauseThread() {
            synchronized (lock) {
                try {
                    if (pause) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void run() {
            while (universe.getAliveCellsCount() > 0 && universe.getGenerationNumber() < MAX_NUM_GENERATIONS) {
                universe.moveGenerations();
                generationLabel.setText("Generation #" + universe.getGenerationNumber());
                aliveLabel.setText("Alive: " + universe.getAliveCellsCount());
                field.update();
                try {
                    Thread.sleep((long) (MAX_GAME_SPEED - gameSpeed) * (MAX_GAME_SPEED / 4) + MAX_GAME_SPEED * 2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pauseThread();
            }
        }
    }
}