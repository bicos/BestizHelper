package com.pockru.bestizhelper.data;

import com.pockru.network.BestizUrlConstants;

import java.util.HashMap;

public class CommonData {

	private static CommonData commonData = null;
	private static HashMap<String, BoardData> map = new HashMap<String, BoardData>();

	public static CommonData getInstance() {
		if (commonData == null) {
			commonData = new CommonData();
		}

		return commonData;
	}

	public HashMap<String, BoardData> getHashData() {
		return map;
	}

	private CommonData() {

		BoardData data;

		data = new BoardData(Constants.GEACHON_ID, "게스트천국", BestizUrlConstants.SERVER_01_URL, "");
		map.put(Constants.GEACHON_ID, data);

		data = new BoardData(Constants.GEAJAP_ID, "게천잡담", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEAJAP_ID, data);

		data = new BoardData(Constants.GEABOM_ID, "게잡의봄", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEABOM_ID, data);

		data = new BoardData(Constants.GEAYEO_ID, "게잡의여름", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEAYEO_ID, data);

		data = new BoardData(Constants.GEAMOT_ID, "게잡멋쟁이", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEAMOT_ID, data);

		data = new BoardData(Constants.GEASU_ID, "게잡스포츠", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEASU_ID, data);

		data = new BoardData(Constants.GEAJIC_ID, "게잡직딩방", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEAJIC_ID, data);

		data = new BoardData(Constants.YEONJAP_ID, "연예잡담", BestizUrlConstants.SERVER_03_URL, "");
		map.put(Constants.YEONJAP_ID, data);

		data = new BoardData(Constants.HATBIT_ID, "연잡햇빛", BestizUrlConstants.SERVER_03_URL, "");
		map.put(Constants.HATBIT_ID, data);

		data = new BoardData(Constants.GIMYO_ID, "게잡기묘", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GIMYO_ID, data);

		data = new BoardData(Constants.USER_MUSIC_ID, "회원노래", BestizUrlConstants.SERVER_05_URL, "");
		map.put(Constants.USER_MUSIC_ID, data);

		data = new BoardData(Constants.DUBANG_ID, "드라마방", BestizUrlConstants.SERVER_04_URL, "");
		map.put(Constants.DUBANG_ID, data);

		data = new BoardData(Constants.GJAK_ID, "게잡알콩", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJAK_ID, data);

		data = new BoardData(Constants.GJKB_ID, "게잡공부방", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJKB_ID, data);

		data = new BoardData(Constants.GJAD_ID, "게잡아이돌", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJAD_ID, data);

		data = new BoardData(Constants.GJHI_ID, "게잡호이", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJHI_ID, data);

		data = new BoardData(Constants.MOVIE_ID, "영화감상", BestizUrlConstants.SERVER_04_URL, "");
		map.put(Constants.MOVIE_ID, data);

		data = new BoardData(Constants.MUSIC_ID, "게천뮤직", BestizUrlConstants.SERVER_05_URL, "");
		map.put(Constants.MUSIC_ID, data);

		data = new BoardData(Constants.JSTAR, "J Star", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.JSTAR, data);

		data = new BoardData(Constants.GJGAME, "게잡오락실", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJGAME, data);

		data = new BoardData(Constants.HEAD, "게잡해드", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.HEAD, data);

		data = new BoardData(Constants.NABI, "봄나비", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.NABI, data);

		data = new BoardData(Constants.GBYD, "게봄양도", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GBYD, data);

		data = new BoardData(Constants.GBYC, "게봄요청", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GBYC, data);

		data = new BoardData(Constants.GB_EVENT, "게봄이벤트", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GB_EVENT, data);

		data = new BoardData(Constants.GY_HB, "해변가", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GY_HB, data);

		data = new BoardData(Constants.GY_YD, "게여양도", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GY_YD, data);

		data = new BoardData(Constants.YJ_SHINEE, "샤이니", BestizUrlConstants.SERVER_03_URL, "");
		map.put(Constants.YJ_SHINEE, data);

		data = new BoardData(Constants.GEABAL_ID, "게잡발코니", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GEABAL_ID, data);

		data = new BoardData(Constants.JICCHIN_ID, "직딩친목방", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.JICCHIN_ID, data);

		data = new BoardData(Constants.YJ_EXO, "EXO", BestizUrlConstants.SERVER_03_URL, "");
		map.put(Constants.YJ_EXO, data);

		data = new BoardData(Constants.YJ_INF, "인피니트", BestizUrlConstants.SERVER_03_URL, "");
		map.put(Constants.YJ_INF, data);

		data = new BoardData(Constants.YJ_MBQ, "엠블랙", BestizUrlConstants.SERVER_03_URL, "");
		map.put(Constants.YJ_MBQ, data);

		data = new BoardData(Constants.GJ_TEST, "게잡테스트", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJ_TEST, data);

		data = new BoardData(Constants.DOLMENGYEE, "돌멩이", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.DOLMENGYEE, data);

		data = new BoardData(Constants.GI_EVENT, "게아이벤트", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GI_EVENT, data);

		data = new BoardData(Constants.GI_YC, "게아요청", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GI_YC, data);

		data = new BoardData(Constants.GI_YD, "게아양도", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GI_YD, data);

		data = new BoardData(Constants.GJ_WC, "게잡월드컵", BestizUrlConstants.SERVER_02_URL, "");
		map.put(Constants.GJ_WC, data);
	}
}
