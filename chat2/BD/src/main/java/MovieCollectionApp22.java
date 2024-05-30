import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class MovieCollectionApp22 extends JFrame {
    private JTextField titleField;
    private JTextField yearField;
    private JTextField directorField;
    private JTextArea descriptionArea;
    private JList<String> movieList;
    private DefaultListModel<String> listModel;

    private Connection connection;

    public MovieCollectionApp22() {
        setTitle("Приложение для управления коллекцией фильмов");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Панель для ввода данных о фильме
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Название:"));
        titleField = new JTextField();
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Год:"));
        yearField = new JTextField();
        inputPanel.add(yearField);
        inputPanel.add(new JLabel("Режиссер:"));
        directorField = new JTextField();
        inputPanel.add(directorField);
        inputPanel.add(new JLabel("Описание:"));
        descriptionArea = new JTextArea();
        descriptionArea.setRows(3);
        inputPanel.add(new JScrollPane(descriptionArea));
        add(inputPanel, BorderLayout.NORTH);

        // Кнопки для взаимодействия с фильмами
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Добавить фильм");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMovie();
            }
        });
        buttonPanel.add(addButton);

        JButton updateButton = new JButton("Обновить фильм");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMovie();
            }
        });
        buttonPanel.add(updateButton);

        JButton deleteButton = new JButton("Удалить фильм");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteMovie();
            }
        });
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Список фильмов
        listModel = new DefaultListModel<>();
        movieList = new JList<>(listModel);
        movieList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int index = movieList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        movieList.setSelectedIndex(index);
                        showPopupMenu(e.getX(), e.getY());
                    }
                }
            }
        });
        add(new JScrollPane(movieList), BorderLayout.SOUTH);

        // Подключение к базе данных
        connectToDatabase();

        displayMovies();

        setVisible(true);
    }

    private void showPopupMenu(int x, int y) {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Изменить");
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedMovie = movieList.getSelectedValue();
                if (selectedMovie != null) {
                    String[] movieDetails = selectedMovie.split(" - ");
                    titleField.setText(movieDetails[0]);
                    yearField.setText(movieDetails[1]);
                    directorField.setText(movieDetails[2]);
                    descriptionArea.setText(getMovieDescription(movieDetails[0]));
                }
            }
        });
        popupMenu.add(editItem);

        JMenuItem viewDescriptionItem = new JMenuItem("Посмотреть описание");
        viewDescriptionItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedMovie = movieList.getSelectedValue();
                if (selectedMovie != null) {
                    String[] movieDetails = selectedMovie.split(" - ");
                    String description = getMovieDescription(movieDetails[0]);
                    JOptionPane.showMessageDialog(MovieCollectionApp22.this, description, "Описание фильма", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        popupMenu.add(viewDescriptionItem);

        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteMovie();
            }
        });
        popupMenu.add(deleteItem);

        popupMenu.show(movieList, x, y);
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

    private void connectToDatabase() {
        String url = "jdbc:mysql://localhost:3306/movie_collection";
        String username = "root";
        String password = "55646504";

        try {
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Подключение к базе данных установлено.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMovie() {
        String title = titleField.getText();
        String yearText = yearField.getText();
        String director = directorField.getText();
        String description = descriptionArea.getText();

        try {
            int year = Integer.parseInt(yearText);
            String sql = "INSERT INTO movies (title, year, director, description) VALUES (?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, title);
                statement.setInt(2, year);
                statement.setString(3, director);
                statement.setString(4, description);
                statement.executeUpdate();
                System.out.println("Фильм успешно добавлен.");
                displayMovies();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверное значение года. Пожалуйста, введите корректное целое число.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateMovie() {
        String title = titleField.getText();
        String yearText = yearField.getText();
        String director = directorField.getText();
        String description = descriptionArea.getText();

        try {
            int year = Integer.parseInt(yearText);
            String sql = "UPDATE movies SET year = ?, director = ?, description = ? WHERE title = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, year);
                statement.setString(2, director);
                statement.setString(3, description);
                statement.setString(4, title);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Фильм успешно обновлен.");
                } else {
                    System.out.println("Фильм не найден.");
                }
                displayMovies();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Неверное значение года. Пожалуйста, введите корректное целое число.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteMovie() {
        String selectedMovie = movieList.getSelectedValue();
        if (selectedMovie != null) {
            String[] movieDetails = selectedMovie.split(" - ");
            String title = movieDetails[0];

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

    private void displayMovies() {
        listModel.clear();

        String sql = "SELECT * FROM movies";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String title = resultSet.getString("title");
                int year = resultSet.getInt("year");
                String director = resultSet.getString("director");

                String movieInfo = title + " - " + year + " - " + director;
                listModel.addElement(movieInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MovieCollectionApp22();
            }
        });
    }
}