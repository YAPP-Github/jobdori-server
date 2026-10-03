package com.jobdori.core.domain.experience

// 경험 내용을 AI 프롬프트 입력용 텍스트로 만든다. 경험을 프롬프트에 넣는 곳이 공유한다.
object ExperiencePromptText {

    fun contentsOf(experience: Experience): String = when (val c = experience.contents) {
        is StarExperienceContents -> "상황: ${c.situation}\n과제: ${c.task}\n행동: ${c.action}\n결과: ${c.result}"
        is FreeExperienceContents -> c.content
    }
}
