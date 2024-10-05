package com.ssafy.drcha.chat.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.ssafy.drcha.chat.dto.ChatMessageParam;
import com.ssafy.drcha.chat.entity.ChatMessage;
import com.ssafy.drcha.chat.repository.ChatMessageRepository;
import com.ssafy.drcha.chat.repository.ChatRoomMemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMongoService {
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;

	/*
	  메시지 저장
	 */
	public ChatMessage saveChatMessage(ChatMessage chatMessage) {
		chatMessageRepository.save(chatMessage);
		return chatMessage;
	}

	/*
  	 대화 내용 AI서버로
 	*/
	public String getConversationByChatRoomId(Long chatRoomId) {
		List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAt(
			String.valueOf(chatRoomId), PageRequest.of(0, 30));
		return messages.stream()
			.map(ChatMessage::getContent)
			.collect(Collectors.joining("\n"));
	}

	/*
	 무한 스크롤
	 */
	public Page<ChatMessage> getChatScrollMessages(String chatRoomId, ChatMessageParam param) {
		int page = param.getPage() != 0 ? param.getPage() : 0;
		int size = param.getSize() != 0 ? param.getSize() : 20;
		return chatMessageRepository.findByRoomIdWithPaging(chatRoomId, page, size);
	}

}