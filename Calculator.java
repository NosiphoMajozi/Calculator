import java.swing.*;

public class Calculator{

   public static void main(String[] args){
   
   }
   
   public static void createAndShowGUI(){
   
   // creating components
   JFrame frame = new Frame("Calculator");
   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   
   int cols, rows;
   
   JPanel mainPanel = new JPanel(new BorderLayout());
   JPanel panel2 = new JPanel(new GridLayout(5, 4, 5, 5));
   
   JTextField display = new JTextField();
   mainPanel.add(display, BorderLayout.NORTH);
   
   // add buttons
   panel2.add(new JButton("7"));
   panel2.add(new JButton("8"));
   panel2.add(new JButton("9"));
   panel2.add(new JButton("/"));
   panel2.add(new JButton("4"));
   panel2.add(new JButton("5"));
   panel2.add(new JButton("6"));
   panel2.add(new JButton("x"));
   panel2.add(new JButton("1"));
   panel2.add(new JButton("2"));
   panel2.add(new JButton("3"));
   panel2.add(new JButton("-"));
   panel2.add(new JButton("0"));
   panel2.add(new JButton("."));
   panel2.add(new JButton("="));
   panel2.add(new JButton("+"));
   panel2.add(new JButton("C"));
   panel2.add(new JButton("CE"));
   
   mainPanel.add(panel2, BorderLayout.CENTER);
   
   
   }
}