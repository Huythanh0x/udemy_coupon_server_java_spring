package com.thanh0x.coursedeal.service

import com.thanh0x.coursedeal.config.logger
import com.thanh0x.coursedeal.dto.CurriculumDTO
import com.thanh0x.coursedeal.dto.CurriculumItemDTO
import com.thanh0x.coursedeal.dto.CurriculumSectionDTO
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.springframework.stereotype.Component

/**
 * Parses course curriculum/syllabus data from the Udemy landing-components response.
 */
@Component
class CurriculumParser {
    private val log = logger()

    fun parse(landingComponents: JSONObject): CurriculumDTO? =
        try {
            val data = landingComponents.optJSONObject("curriculum_context")?.optJSONObject("data")
            data?.let {
                CurriculumDTO(
                    sections = parseSections(it.optJSONArray("sections")),
                    totalDuration = it.optString("estimated_content_length_text", ""),
                    totalDurationSeconds = it.optInt("estimated_content_length_in_seconds", 0),
                    totalLectures = it.optInt("num_of_published_lectures", 0),
                )
            }
        } catch (e: JSONException) {
            log.error("Error parsing curriculum", e)
            null
        }

    private fun parseSections(sectionsArray: JSONArray?): List<CurriculumSectionDTO> =
        sectionsArray?.let { arr ->
            (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::parseSection) }
        } ?: emptyList()

    private fun parseSection(sectionObj: JSONObject): CurriculumSectionDTO =
        CurriculumSectionDTO(
            title = sectionObj.optString("title", ""),
            index = sectionObj.optInt("index", 0),
            duration = sectionObj.optString("content_length_text", ""),
            durationSeconds = sectionObj.optInt("content_length", 0),
            lectureCount = sectionObj.optInt("lecture_count", 0),
            items = parseItems(sectionObj.optJSONArray("items")),
        )

    private fun parseItems(itemsArray: JSONArray?): List<CurriculumItemDTO> =
        itemsArray?.let { arr ->
            (0 until arr.length()).mapNotNull { i -> arr.optJSONObject(i)?.let(::parseItem) }
        } ?: emptyList()

    private fun parseItem(itemObj: JSONObject): CurriculumItemDTO =
        CurriculumItemDTO(
            id = itemObj.optLong("id", 0),
            title = itemObj.optString("title", ""),
            description = itemObj.optString("description", ""),
            contentSummary = itemObj.optString("content_summary", ""),
            itemType = itemObj.optString("item_type", ""),
            canBePreviewed = itemObj.optBoolean("can_be_previewed", false),
            isCodingExercise = itemObj.optBoolean("is_coding_exercise", false),
            isPracticeTest = itemObj.optBoolean("is_practice_test", false),
            previewUrl = itemObj.optString("preview_url", ""),
            learnUrl = itemObj.optString("learn_url", ""),
            objectIndex = itemObj.optInt("object_index", 0),
        )
}
