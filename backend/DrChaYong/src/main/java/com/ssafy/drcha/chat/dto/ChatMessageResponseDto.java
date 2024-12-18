package com.ssafy.drcha.chat.dto;

import java.time.LocalDateTime;

import com.ssafy.drcha.chat.entity.ChatMessage;
import com.ssafy.drcha.chat.enums.ChatMessageType;
import com.ssafy.drcha.iou.dto.IouPdfResponseDto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ChatMessageResponseDto {
	private String id;  // id를 Long 타입으로 변경
	private Long chatRoomId;  // chatRoomId를 Long 타입으로 변경
	private String content;
	private Long senderId;  // senderId를 Long 타입으로 변경
	private ChatMessageType messageType;
	private LocalDateTime createdAt;
	private IouPdfResponseDto iouInfo;

	public static ChatMessageResponseDto from(ChatMessage chatMessage) {
		return new ChatMessageResponseDto(
			chatMessage.getId(),
			convertStringToLong(chatMessage.getChatRoomId()),
			chatMessage.getContent(),
			convertStringToLong(chatMessage.getSenderId()),
			chatMessage.getMessageType(),
			chatMessage.getCreatedAt(),
			chatMessage.getIouInfo()
		);
	}

	private static Long convertStringToLong(String value) {
		if (value == null) {
			return 0L;
		}
		return Long.parseLong(value);

	}

}
