package test.mcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) throws IOException {
        System.out.println("start p");
        List<String> list = new ArrayList<>();
        list.add("E:/files/project/NeuraCraft/NeuraCraft-main/src/test/mcp_server/main.py");
        list.add("--local-timezone=Asia/Shanghai");
        list.add(0 ,"python");
        ProcessBuilder pb = new ProcessBuilder(list);
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        PrintWriter writer = new PrintWriter(p.getOutputStream(), true);
        Scanner sc = new Scanner(System.in);
        
        Thread readeLine = new Thread(() -> {
            try {
                while (true) {
                    String line = reader.readLine();
                    System.out.println("readeLine: "+line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        Thread readErrorLine = new Thread(() -> {
            try {
                while (true) {
                    String line = errorReader.readLine();
                    System.out.println("readErrorLine: "+line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        readeLine.start();
        readErrorLine.start();
        
        while (true) {
            System.out.println("input start");
            String inLine = sc.nextLine();
            if (inLine.equals("exit"))
                break;
            writer.println(inLine);
            writer.flush();
            System.out.println("input end");
        }
    }
}
