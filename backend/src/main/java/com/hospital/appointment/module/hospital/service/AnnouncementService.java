package com.hospital.appointment.module.hospital.service;

import com.hospital.appointment.module.hospital.model.Announcement;

import java.util.List;

public interface AnnouncementService {
    List<Announcement> getPublishedList();
    Announcement getDetail(Long id);
    List<Announcement> getAllList(int page, int size);
    Announcement save(Announcement announcement);
    void update(Announcement announcement);
    void delete(Long id);
}
