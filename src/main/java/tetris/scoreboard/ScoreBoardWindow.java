package tetris.scoreboard;

import javax.swing.*;
import java.util.List;

public class ScoreBoardWindow {
    public void show() {
        List<ScoreRecord> scores = new ScoreLoader().loadScores();
        scores.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        List<ScoreRecord> top3 = scores.subList(0, Math.min(3, scores.size()));

        String[] columns = {"순위", "이름", "점수"};
        Object[][] data = new Object[top3.size()][3];

        for (int i = 0; i < top3.size(); i++) {
            ScoreRecord record = top3.get(i);
            data[i][0] = i + 1;
            data[i][1] = record.getName();
            data[i][2] = record.getScore();
        }

        JTable table = new JTable(data, columns);
        table.setRowHeight(30);

        JFrame frame = new JFrame("Top 3 Scores");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new JScrollPane(table));
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
