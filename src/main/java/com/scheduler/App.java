package com.scheduler;

import com.scheduler.auth.AuthService;
import com.scheduler.model.Department;
import com.scheduler.model.Room;
import com.scheduler.model.RoomType;
import com.scheduler.storage.FileStorage;
import com.scheduler.storage.StorageService;
import com.scheduler.util.DataSeeder;
import com.scheduler.view.SplashView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private static FileStorage storage;
    private static AuthService authService;

    public static FileStorage getStorage() {
        return storage;
    }

    public static AuthService getAuthService() {
        return authService;
    }

    @Override
    public void start(Stage stage) {
        // Initialize storage and auth
        storage = new FileStorage();
        authService = new AuthService(storage);

        // Seed base data
        seedInitialData();

        // Show splash screen
        SplashView splash = new SplashView(storage, authService, stage);
        Scene scene = new Scene(splash.getRoot(), 900, 600);

        stage.setTitle("Smart Academic Scheduler");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void seedInitialData() {
        seedDepartments();
        seedRooms();
    }

    private void seedDepartments() {
        List<Department> existing = storage.loadDepartments();
        if (!existing.isEmpty()) return;

        String[][] depts = {
                {"CST", "Computer Science & Technology"},
                {"EET", "Electrical Engineering Technology"},
                {"CEE", "Civil Engineering Technology"},
                {"MET", "Mechanical Engineering Technology"},
                {"RAC", "Refrigeration & Air Conditioning"},
                {"AUT", "Automobile Engineering"},
                {"FDT", "Food Technology"},
                {"TET", "Textile Engineering Technology"},
                {"CMT", "Chemical Technology"},
                {"ARC", "Architecture & Interior Design"},
                {"ELT", "Electronics Technology"},
                {"POW", "Power Engineering Technology"}
        };

        List<Department> list = new ArrayList<>();
        for (String[] d : depts) {
            list.add(new Department(d[0], d[1]));
        }

        storage.saveDepartments(list);
    }

    private void seedRooms() {
        List<Room> existing = storage.loadRooms();
        if (!existing.isEmpty()) return;

        List<Room> rooms = new ArrayList<>();

        // Floors 1–20, 8 rooms each
        for (int floor = 1; floor <= 20; floor++) {
            for (int room = 1; room <= 8; room++) {
                Room r = new Room();
                r.setRoomNo("NB-" + floor + "0" + room);
                r.setFloor(floor);

                if (room >= 7) {
                    // Labs
                    r.setRoomType(RoomType.LAB);
                    r.setCapacity(40);
                    r.setTotalPCs(40);
                    r.setActivePCs(38);
                    r.setHasProjector(true);
                } else {
                    // Theory rooms
                    r.setRoomType(RoomType.THEORY);
                    r.setCapacity(60);
                    r.setHasProjector(room <= 4);
                    r.setHasSoundSystem(room <= 2);
                }

                r.setAvailable(true);
                rooms.add(r);
            }
        }

        storage.saveRooms(rooms);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
