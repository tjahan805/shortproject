import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderApp {
  private JFrame frame;
  private JTextField titleField;
  private JTextArea descriptionArea;
  private JTextField dateField;
  private DefaultListModel<Reminder> reminderListModel;
  private ScheduledExecutorService scheduler;

  public ReminderApp() {
    frame = new JFrame("BTHS Reminder App");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel(new BorderLayout());
    frame.getContentPane().add(panel, BorderLayout.CENTER);

    reminderListModel = new DefaultListModel<>();
    JList<Reminder> reminderList = new JList<>(reminderListModel);
    reminderList.setCellRenderer(new ReminderListCellRenderer());
    JScrollPane scrollPane = new JScrollPane(reminderList);
    panel.add(scrollPane, BorderLayout.CENTER);

    JPanel formPanel = new JPanel(new GridLayout(4, 2));
    panel.add(formPanel, BorderLayout.SOUTH);

    JLabel titleLabel = new JLabel("Subject:");
    titleField = new JTextField();
    formPanel.add(titleLabel);
    formPanel.add(titleField);

    JLabel descriptionLabel = new JLabel("Description:");
    descriptionArea = new JTextArea();
    descriptionArea.setRows(3);
    JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
    formPanel.add(descriptionLabel);
    formPanel.add(descriptionScrollPane);

    JLabel dateLabel = new JLabel("Date (MM/DD/YYYY HH:mm):");
    dateField = new JTextField();
    formPanel.add(dateLabel);
    formPanel.add(dateField);

    JButton addButton = new JButton("Add Reminder");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        String dateString = dateField.getText();

        Date date = parseDate(dateString);
        if (date != null) {
          Reminder reminder = new Reminder(title, description, date);
          reminderListModel.addElement(reminder);

          scheduleReminder(reminder);

          titleField.setText("");
          descriptionArea.setText("");
          dateField.setText("");
        } else {
          JOptionPane.showMessageDialog(frame, "Invalid date format. Please use the format: MM/DD/YYYY HH:mm");
        }
      }
    });
    formPanel.add(addButton);

    frame.setSize(400, 300);
    frame.setVisible(true);

    scheduler = Executors.newScheduledThreadPool(1);
  }

  private Date parseDate(String dateString) {
    try {
      return new SimpleDateFormat("MM/dd/yyyy HH:mm").parse(dateString);
    } catch (ParseException e) {
      return null;
    }
  }

  private void scheduleReminder(Reminder reminder) {
    long delayMillis = reminder.getDate().getTime() - System.currentTimeMillis();
    if (delayMillis > 0) {
      scheduler.schedule(() -> {
        reminderListModel.removeElement(reminder);
        JOptionPane.showMessageDialog(frame, "Reminder for \"" + reminder.getTitle() + "\" has expired.");
      }, delayMillis, TimeUnit.MILLISECONDS);
    }
  }

  private class ReminderListCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof Reminder) {
        Reminder reminder = (Reminder) value;
        setText(reminder.getTitle());
      }
      return this;
    }
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new ReminderApp();
      }
    });
  }
}