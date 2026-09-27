package tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import tetris.scoreboard.ScoreBoardWindow;

public class StartScreen {
    private JFrame frame;//창 객체
    private JButton[] buttons;//버튼 배열
    private int selectedIndex = 0;//선택된 버튼 인덱스(초기는 첫번째인 0 선택)
    private void updateSelection() {
        for (int i = 0; i < buttons.length; i++) {
        if (i == selectedIndex) {//선택된 버튼이면
            buttons[i].setBackground(new Color(80, 160, 255));//선택된 버튼 배경색 파란색으로 설정
            buttons[i].setForeground(Color.WHITE);//선택된 버튼 글자색 흰색으로 설정
        } else {//선택되지 않은 버튼이면
            buttons[i].setBackground(new Color(220, 220, 220));//선택되지 않은 버튼 배경색 회색으로 설정
            buttons[i].setForeground(Color.BLACK);//선택되지 않은 버튼 글자색 검은색으로 설정
        }
    }
    }

    public void show() {
        frame = new JFrame("Tetris");//창 제목
        frame.setSize(700, 800);//창 사이즈
        frame.setResizable(false);//창 크기 조절 불가
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//창 닫기 버튼 클릭 시 프로그램 종료
        frame.setLocationRelativeTo(null);//창을 화면 가운데에 배치
        frame.getContentPane().setBackground(Color.BLACK);//창 배경색 검은색으로 설정

        JLabel title = new JLabel("TETRIS");//타이틀 라벨 생성
        title.setBounds(245, 60, 210, 80);//글자위치, 크기 설정
        title.setForeground(Color.WHITE);//글자색 흰색으로 설정
        title.setFont(new Font("SansSerif", Font.BOLD, 36));//글자 폰트, 스타일, 크기 설정)
        title.setHorizontalAlignment(JLabel.CENTER);//글자 가운데 정렬

        JButton startButton = new JButton("게임시작");//게임 시작 버튼
        startButton.setBounds(300, 250, 100, 50);//버튼 위치와 크기 설정
        startButton.addActionListener(e -> {
            System.out.println("게임 시작 버튼 누름");//버튼 클릭 시 버튼 누름 출력
        });

        JButton settingButton = new JButton("설정");//설정 버튼
        settingButton.setBounds(300, 450, 100, 50);//버튼 위치와 크기 설정
        settingButton.addActionListener(e -> {
            System.out.println("설정 버튼 누름");//버튼 클릭 시 설정 버튼 누름 출력
        });

        JButton rankingButton = new JButton("스코어 보드");//스코어 보드 버튼
        rankingButton.setBounds(300, 350, 100, 50);//버튼 위치와 크기 설정
        rankingButton.addActionListener(e -> {
            new ScoreBoardWindow().show();//스코어 보드 창 표시
        });

        JButton exitButton = new JButton("게임 종료");//게임 종료 버튼
        exitButton.setBounds(300, 550, 100, 50);//버튼 위치와 크기 설정
        exitButton.addActionListener(e -> {
            System.exit(0);//버튼 클릭 시 프로그램 종료
        });

        //버튼 배열 초기화
        buttons = new JButton[] {
            startButton, rankingButton, settingButton, exitButton
        };
        
        //마우스로도 현재 클릭 가능한 버튼 표시
        for (int i = 0; i < buttons.length; i++) {//버튼 배열 순회
            final int index = i;//현재 버튼 인덱스를 final로 선언하여 내부 클래스에서 사용 가능하게 함
            buttons[i].addMouseListener(new MouseAdapter() {//마우스 이벤트 처리
                @Override
                public void mouseEntered(MouseEvent e) {//마우스가 버튼 위로 올라갔을 때
                    selectedIndex = index;//선택된 버튼 인덱스를 현재 버튼 인덱스로 설정
                    updateSelection();//버튼 선택 상태 업데이트
                }
            });
        }

        updateSelection();//처음 게임 실행 시 버튼 선택 상태 업데이트

        frame.add(title);//타이틀 라벨을 창에 추가
        frame.add(startButton);//게임 시작 버튼을 창에 추가
        frame.add(settingButton);//설정 버튼을 창에 추가
        frame.add(rankingButton);//스코어 보드 버튼을 창에 추가
        frame.add(exitButton);//게임 종료 버튼을 창에 추가

        frame.setLayout(null);//레이아웃 매니저를 null로 설정하여 절대 위치 사용

        JRootPane root = frame.getRootPane();//루트 팬을 가져옴
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);//키 입력을 받을 수 있도록 설정
        ActionMap actionMap = root.getActionMap();//키 입력에 대한 동작을 정의할 수 있도록 설정

        inputMap.put(KeyStroke.getKeyStroke("UP"), "moveUp");
        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "select");

        // Up 키를 눌렀을 때 선택된 버튼 인덱스 감소
        actionMap.put("moveUp", new AbstractAction(){
            @Override 
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex > 0){
                    selectedIndex--;
                    updateSelection();
                }
            }
        });

        // Down 키를 눌렀을 때 선택된 버튼 인덱스 증가
        actionMap.put("moveDown", new AbstractAction(){
            @Override 
            public void actionPerformed(ActionEvent e) {
                if (selectedIndex < buttons.length - 1){
                    selectedIndex++;
                    updateSelection();
                }
            }
        });

        // Enter 키를 눌렀을 때 선택된 버튼 클릭 이벤트 발생
        actionMap.put("select", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                buttons[selectedIndex].doClick();
            }
        });

        frame.setVisible(true);//창을 화면에 표시
    }
}