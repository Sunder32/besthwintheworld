import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class MovieCollectionApp extends JFrame {
    private JTextField titleField;
    private JTextField yearField;
    private JTextField directorField;
    private JTextField genreField;
    private JTextArea descriptionArea;
    private JTable movieTable;
    private DefaultTableModel tableModel;
    private JCheckBox topMovieCheckbox;

    private JPanel mainPanel;
    private JButton addMovieButton;
    private JButton viewAllMoviesButton;
    private JButton topMoviesButton;

    private JPanel currentPanel;

    private Connection connection;

    public MovieCollectionApp() {
        setTitle("Приложение для управления коллекцией фильмов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Создание главного панели
        createMainPanel();
        setContentPane(mainPanel);

        // Подключение к базе данных
        connectToDatabase();

        setVisible(true);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new GridLayout(3, 1, 20, 20));

        // Кнопка "Добавить фильм"
        addMovieButton = new JButton("Добавить фильм");
        addMovieButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMovieInputPanel();
            }
        });
        mainPanel.add(addMovieButton);

        // Кнопка "Просмотреть все фильмы"
        viewAllMoviesButton = new JButton("Просмотреть все фильмы");
        viewAllMoviesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMovieListPanel();
            }
        });
        mainPanel.add(viewAllMoviesButton);

        // Кнопка "Топ 10 лучших фильмов"
        topMoviesButton = new JButton("Топ 10 лучших фильмов");
        topMoviesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTopMoviesPanel();
            }
        });
        mainPanel.add(topMoviesButton);

        currentPanel = mainPanel;
    }

    private void showMovieInputPanel() {
        showMovieInputPanel("", 0, "", "", "", false);
    }

    private void showMovieInputPanel(String title, int year, String director, String genre, String description, boolean isTopMovie) {
        JPanel inputPanel = createInputPanel();
        titleField.setText(title);
        yearField.setText(String.valueOf(year));
        directorField.setText(director);
        genreField.setText(genre);
        descriptionArea.setText(description);
        topMovieCheckbox.setSelected(isTopMovie);
        setContentPane(inputPanel);
        revalidate();
        repaint();
        currentPanel = inputPanel;
    }

    private void showMovieListPanel() {
        JPanel listPanel = createListPanel();
        setContentPane(listPanel);
        revalidate();
        repaint();
        currentPanel = listPanel;
    }

    private void showTopMoviesPanel() {
        JPanel topMoviesPanel = createTopMoviesPanel();
        setContentPane(topMoviesPanel);
        revalidate();
        repaint();
        currentPanel = topMoviesPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 5);

        constraints.gridx = 0;
        constraints.gridy = 0;
        inputPanel.add(new JLabel("Название:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        titleField = new JTextField(20);
        inputPanel.add(titleField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        inputPanel.add(new JLabel("Год:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        yearField = new JTextField(20);
        inputPanel.add(yearField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        inputPanel.add(new JLabel("Режиссер:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        directorField = new JTextField(20);
        inputPanel.add(directorField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        inputPanel.add(new JLabel("Жанр:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        genreField = new JTextField(20);
        inputPanel.add(genreField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        inputPanel.add(new JLabel("Описание:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 4;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        inputPanel.add(new JScrollPane(descriptionArea), constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        inputPanel.add(new JLabel("Топ-10 фильмов:"), constraints);

        constraints.gridx = 1;
        constraints.gridy = 5;
        topMovieCheckbox = new JCheckBox();
        inputPanel.add(topMovieCheckbox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        JButton addButton = new JButton("Добавить фильм");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateMovieInput()) {
                    addMovie();
                }
            }
        });
        inputPanel.add(addButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainPanel();
            }
        });
        inputPanel.add(backButton, constraints);

        return inputPanel;
    }

    private boolean validateMovieInput() {
        String title = titleField.getText().trim();
        String yearText = yearField.getText().trim();
        String director = directorField.getText().trim();
        String genre = genreField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (title.isEmpty() || yearText.isEmpty() || director.isEmpty() || genre.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, заполните все поля.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Integer.parseInt(yearText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверное значение года. Пожалуйста, введите корректное целое число.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private JPanel createListPanel() {
        JPanel listPanel = new JPanel(new BorderLayout());

        String[] columns = {"Название", "Год", "Режиссер", "Жанр", "Описание"};
        tableModel = new DefaultTableModel(columns, 0);
        movieTable = new JTable(tableModel);
        movieTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = movieTable.rowAtPoint(e.getPoint());
                    movieTable.setRowSelectionInterval(row, row);
                    showPopupMenu(e);
                }
            }
        });
        listPanel.add(new JScrollPane(movieTable), BorderLayout.CENTER);

        displayMovies();

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainPanel();
            }
        });
        listPanel.add(backButton, BorderLayout.SOUTH);

        return listPanel;
    }

    private JPanel createTopMoviesPanel() {
        JPanel topMoviesPanel = new JPanel(new BorderLayout());

        String[] columns = {"Название", "Год", "Режиссер", "Жанр", "Описание"};
        tableModel = new DefaultTableModel(columns, 0);
        movieTable = new JTable(tableModel);
        topMoviesPanel.add(new JScrollPane(movieTable), BorderLayout.CENTER);

        displayTopMovies();

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMainPanel();
            }
        });
        topMoviesPanel.add(backButton, BorderLayout.SOUTH);

        return topMoviesPanel;
    }

    private void showMainPanel() {
        setContentPane(mainPanel);
        revalidate();
        repaint();
        currentPanel = mainPanel;
    }

    private void connectToDatabase() {
        String url = "jdbc:mysql://it.vshp.online:3306/db_ac68d6";
        String username = "st_ac68d6";
        String password = "26ae8c5ee970";

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Подключение к базе данных установлено.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMovie() {
        String title = titleField.getText().trim();
        String yearText = yearField.getText().trim();
        String director = directorField.getText().trim();
        String genre = genreField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean isTopMovie = topMovieCheckbox.isSelected();

        try {
            int year = Integer.parseInt(yearText);
            String sql = "INSERT INTO movies (title, year, director, genre, description, is_top_movie) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, title);
                statement.setInt(2, year);
                statement.setString(3, director);
                statement.setString(4, genre);
                statement.setString(5, description);
                statement.setBoolean(6, isTopMovie);
                statement.executeUpdate();
                System.out.println("Фильм успешно добавлен.");
                clearMovieFields();
                tableModel = (DefaultTableModel) movieTable.getModel();
                displayMovies();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearMovieFields() {
        titleField.setText("");
        yearField.setText("");
        directorField.setText("");
        genreField.setText("");
        descriptionArea.setText("");
        topMovieCheckbox.setSelected(false);
    }

    private void displayMovies() {
        tableModel.setRowCount(0);

        String sql = "SELECT * FROM movies";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                int year = resultSet.getInt("year");
                String director = resultSet.getString("director");
                String genre = resultSet.getString("genre");
                String description = resultSet.getString("description");

                Object[] rowData = {title, year, director, genre, description};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayTopMovies() {
        tableModel.setRowCount(0);

        String sql = "SELECT * FROM movies WHERE is_top_movie = true ORDER BY year DESC LIMIT 10";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                int year = resultSet.getInt("year");
                String director = resultSet.getString("director");
                String genre = resultSet.getString("genre");
                String description = resultSet.getString("description");

                Object[] rowData = {title, year, director, genre, description};
                tableModel.addRow(rowData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getMovieDescription(String title) {
        String sql = "SELECT description FROM movies WHERE title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("description");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isMovieTopMovie(String title) {
        String sql = "SELECT is_top_movie FROM movies WHERE title = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("is_top_movie");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void deleteMovie() {
        int selectedRow = movieTable.getSelectedRow();
        if (selectedRow != -1) {
            String title = (String) tableModel.getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this, "Вы уверены, что хотите удалить фильм \"" + title + "\"?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM movies WHERE title = ?";

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, title);
                    int rowsDeleted = statement.executeUpdate();
                    if (rowsDeleted > 0) {
                        System.out.println("Фильм успешно удален.");
                    } else {
                        System.out.println("Фильм не найден.");
                    }
                    displayMovies();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showPopupMenu(MouseEvent e) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Изменить");
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = movieTable.getSelectedRow();
                if (selectedRow != -1) {
                    String title = (String) tableModel.getValueAt(selectedRow, 0);
                    int year = (int) tableModel.getValueAt(selectedRow, 1);
                    String director = (String) tableModel.getValueAt(selectedRow, 2);
                    String genre = (String) tableModel.getValueAt(selectedRow, 3);
                    String description = (String) tableModel.getValueAt(selectedRow, 4);
                    boolean isTopMovie = isMovieTopMovie(title);
                    showMovieInputPanel(title, year, director, genre, description, isTopMovie);
                }
            }
        });
        popupMenu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteMovie();
            }
        });
        popupMenu.add(deleteItem);

        popupMenu.show(movieTable, e.getX(), e.getY());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MovieCollectionApp();
            }
        });
    }
        }