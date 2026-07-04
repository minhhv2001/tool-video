package com.example.tool.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CreativeBriefService {

	public String buildBrief(String topic, int videosToGenerate) {
		String cleanTopic = topic == null || topic.isBlank() ? "untitled topic" : topic.trim();
		List<String> hooks = new ArrayList<>();
		hooks.add("Hook 1: mo dau bang khoanh khac gay to mo trong 1-2 giay dau.");
		hooks.add("Hook 2: cat nhanh vao chi tiet chuyen dong, tranh doan mo dau dai.");
		hooks.add("Hook 3: ket bang cau goi mo de nguoi xem muon xem tiep video sau.");

		String searchPhrase = Normalizer.normalize(cleanTopic, Normalizer.Form.NFD)
				.replace('Đ', 'D')
				.replace('đ', 'd')
				.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9\\s-]", "")
				.replaceAll("\\s+", " ")
				.trim();

		return String.format(Locale.ROOT,
				"Topic: %s%n"
						+ "Search phrase: %s%n"
						+ "Batch idea: tao %d bien the video doc 9:16, moi video xao tron thu tu clip va lay cac diem bat dau khac nhau.%n"
						+ "Structure: hook ngan -> 3 den 6 canh chinh -> ket thuc nhanh.%n"
						+ "%s%n"
						+ "%s%n"
						+ "%s%n"
						+ "Compliance note: chi dung video ban so huu, video do ban quay, hoac stock/CC co giay phep phu hop voi muc dich dang tai.%n",
				cleanTopic, searchPhrase, videosToGenerate, hooks.get(0), hooks.get(1), hooks.get(2));
	}
}
