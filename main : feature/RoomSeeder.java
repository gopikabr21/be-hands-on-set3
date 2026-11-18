package com.roombooking.config;

import com.roombooking.entity.Room;
import com.roombooking.repository.RoomRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RoomSeeder implements ApplicationRunner {

    private final RoomRepository rooms;

    public RoomSeeder(RoomRepository rooms) {
        this.rooms = rooms;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (rooms.count() > 0) {
            return;
        }
        rooms.save(new Room("Atlas", 8, true));
        rooms.save(new Room("Borealis", 6, true));
        rooms.save(new Room("Cedar", 10, true));
        rooms.save(new Room("Drift", 4, true));
        rooms.save(new Room("Ember", 12, true));
        rooms.save(new Room("Nook", 2, false));
    }
}
