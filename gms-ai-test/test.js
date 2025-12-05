import "dotenv/config";
import axios from "axios";

async function testGmsAi() {
  try {
    const url =
      "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions";

    const payload = {
      model: "gpt-4.1-mini",
      messages: [
        { role: "system", content: "Answer in Korean" },
        {
          role: "user",
          content:
            "오늘 섭취한 식단: 아침 토스트 2장, 점심 치킨마요덮밥, 저녁 라면. 분석해줘.",
        },
      ],
      max_tokens: 500,
      temperature: 0.3,
    };

    const headers = {
      "Content-Type": "application/json",
      Authorization: `Bearer ${process.env.GMS_KEY}`,
    };

    const response = await axios.post(url, payload, { headers });

    console.log("===== AI 응답 내용 =====");
    console.log(response.data.choices[0].message.content);

    console.log("\n===== 전체 RAW 응답 =====");
    console.dir(response.data, { depth: 5 });
  } catch (error) {
    console.log("\n❌ ===== 오류 발생 =====");
    if (error.response) {
      console.log(error.response.data);
    } else {
      console.log(error.message);
    }
  }
}

testGmsAi();
