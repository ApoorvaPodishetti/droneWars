import java.io.*;
import java.util.Random;

public class Main {

    // Constants
    static final int ArrakisOrbittime = 12;
    static final int Giediorbittime = 60;
    static final int Alignment = 10;

    static final String TransmissionFile = "trans.mxt";
    static final String AcknowledgementFile = "recvrs.mxt";

    public static void main(String[] args) {
        Thread baseStationThread = new Thread(new BaseStation());
        Thread responderThread = new Thread(new Responder());

        baseStationThread.start();
        responderThread.start();
    }

    static double calculatePosition(int orbitTime, int time) {
        return (360.0 * time) / orbitTime;
    }

    static boolean PlanetsAligned(double arrakisPosition, double giediPrimePosition) {
        double diff = Math.abs(arrakisPosition - giediPrimePosition);
        return diff <= Alignment || diff >= 360 - Alignment;
    }

    static class BaseStation implements Runnable {

        @Override
        public void run() {
            int t = 0;
            Random random = new Random();

            while (true) {
                double arrakisPosition = calculatePosition(ArrakisOrbittime, t);
                double giediPrimePosition = calculatePosition(Giediorbittime, t);

                if (PlanetsAligned(arrakisPosition, giediPrimePosition)) {
                    // Planets are aligned, send a random instruction
                    String instruction = generateRandomInstruction();
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(TransmissionFile))) {
                        writer.write(instruction);
                        System.out.println("Base Station Arrakis: Sending instruction '" + instruction + "' at time " + t + " LTU");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(1000);  
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Base Station Arrakis : Waiting for alignment at time " + t + " LTU");
                }

                t++;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private String generateRandomInstruction() {
            String[] instructions = {"<>><", "</<<>>/", "><<>>><"};
            return instructions[new Random().nextInt(instructions.length)];
        }
    }

    static class Responder implements Runnable {

        @Override
        public void run() {
            int t = 0;

            while (true) {
                double arrakisPosition = calculatePosition(ArrakisOrbittime, t);
                double giediPrimePosition = calculatePosition(Giediorbittime, t);

                if (PlanetsAligned(arrakisPosition, giediPrimePosition)) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(TransmissionFile))) {
                        String instruction = reader.readLine();
                        if (instruction != null && !instruction.isEmpty()) {
                            System.out.println("Responder Giedi Prime: Received instruction '" + instruction + "' at time " + t + " LTU");

                            Thread.sleep(1000);
                        }
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Responder Giedi Prime: No instruction available at time " + t + " LTU");
                    }
                }

            }
        }

    }
}
