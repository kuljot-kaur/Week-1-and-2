import java.util.*;

class ParkingSpot {

    String licensePlate;
    long entryTime;
    Status status;

    public ParkingSpot() {
        status = Status.EMPTY;
    }
}

enum Status {
    EMPTY,
    OCCUPIED,
    DELETED
}

class ParkingLot {

    private ParkingSpot[] table;
    private int capacity;
    private int occupiedSpots = 0;

    private int totalProbes = 0;
    private int totalParkingRequests = 0;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        table = new ParkingSpot[capacity];

        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public void parkVehicle(String licensePlate) {

        totalParkingRequests++;

        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % capacity;
            probes++;
        }

        table[index].licensePlate = licensePlate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        occupiedSpots++;
        totalProbes += probes;

        System.out.println("Assigned spot #" + index +
                " (" + probes + " probes)");
    }

    // Exit vehicle
    public void exitVehicle(String licensePlate) {

        int index = hash(licensePlate);

        while (table[index].status != Status.EMPTY) {

            if (table[index].status == Status.OCCUPIED &&
                    table[index].licensePlate.equals(licensePlate)) {

                long exitTime = System.currentTimeMillis();
                long durationMillis = exitTime - table[index].entryTime;

                double hours = durationMillis / (1000.0 * 60 * 60);
                double fee = Math.ceil(hours) * 5; // $5/hour

                table[index].status = Status.DELETED;
                occupiedSpots--;

                System.out.println("Spot #" + index + " freed");
                System.out.println("Duration: " +
                        String.format("%.2f", hours) + " hours");
                System.out.println("Fee: $" + fee);

                return;
            }

            index = (index + 1) % capacity;
        }

        System.out.println("Vehicle not found.");
    }

    // Find nearest available spot
    public int findNearestSpot() {

        for (int i = 0; i < capacity; i++) {

            if (table[i].status == Status.EMPTY ||
                    table[i].status == Status.DELETED) {

                return i;
            }
        }

        return -1;
    }

    // Statistics
    public void getStatistics() {

        double occupancy = (occupiedSpots * 100.0) / capacity;

        double avgProbes =
                totalParkingRequests == 0 ? 0 :
                        (double) totalProbes / totalParkingRequests;

        System.out.println("\nParking Statistics:");
        System.out.println("Occupancy: " +
                String.format("%.2f", occupancy) + "%");

        System.out.println("Average Probes: " +
                String.format("%.2f", avgProbes));
    }
}

public class SmartParkingSystem {

    public static void main(String[] args) throws InterruptedException {

        ParkingLot lot = new ParkingLot(500);

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        Thread.sleep(2000);

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();

        int nearest = lot.findNearestSpot();
        System.out.println("Nearest available spot: " + nearest);
    }
}