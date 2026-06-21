package com.hospital.appointment.module.message.controller;

import com.hospital.appointment.common.annotation.RequireRole;
import com.hospital.appointment.common.result.R;
import com.hospital.appointment.module.message.service.SiteMessageService;
import com.hospital.appointment.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {

    private final SiteMessageService messageService;

    @GetMapping("/messages")
    public R<?> myMessages(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) {
        return R.ok(messageService.getMyMessages(UserContext.getUserId(), page, size));
    }

    @GetMapping("/unread-count")
    public R<Map<String, Integer>> unreadCount() {
        return R.ok(Map.of("count", messageService.unreadCount(UserContext.getUserId())));
    }

    @PutMapping("/read/{id}")
    public R<Void> markRead(@PathVariable Long id) {
        messageService.markAsRead(id, UserContext.getUserId());
        return R.ok();
    }

    @PostMapping("/send")
    @RequireRole({"SYS_ADMIN", "DEPT_ADMIN"})
    public R<Void> sendSystemMessage(@RequestBody Map<String, Object> body) {
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        messageService.sendSystemMessage(receiverId, title, content);
        return R.okMsg("发送成功");
    }
}
