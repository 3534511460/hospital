package com.hospital.appointment.module.hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.appointment.common.exception.BusinessException;
import com.hospital.appointment.module.hospital.mapper.AnnouncementMapper;
import com.hospital.appointment.module.hospital.model.Announcement;
import com.hospital.appointment.module.hospital.service.AnnouncementService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public List<Announcement> getPublishedList() {
        return announcementMapper.selectPublished();
    }

    @Override
    public Announcement getDetail(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null || a.getPublishStatus() != 1)
            throw BusinessException.notFound("公告不存在");
        a.setViewCount(a.getViewCount() != null ? a.getViewCount() + 1 : 1);
        announcementMapper.updateById(a);
        return a;
    }

    @Override
    public List<Announcement> getAllList(int page, int size) {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        w.orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(new Page<>(page, size), w).getRecords();
    }

    @Override
    public Announcement save(Announcement announcement) {
        announcement.setPublisherId(UserContext.getUserId());
        if (announcement.getPublishStatus() != null && announcement.getPublishStatus() == 1) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
        announcementMapper.insert(announcement);
        return announcement;
    }

    @Override
    public void update(Announcement announcement) {
        if (announcement.getPublishStatus() != null && announcement.getPublishStatus() == 1) {
            announcement.setPublishedAt(LocalDateTime.now());
        }
        announcementMapper.updateById(announcement);
    }

    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }
}
