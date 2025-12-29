$(document).ready(function () {
    const BASE_PATH = "/backend";
    const API_BASE = `${BASE_PATH}/admin/api/surveys`;

    const modal = $("#surveyModal");
    const questionsContainer = $("#surveyQuestions");

    function openModal() {
        console.log("[survey] 설문 등록 모달 열기");
        modal.fadeIn(150);
    }

    function closeModal() {
        modal.fadeOut(150);
    }

    function resetForm() {
        $("#surveyTitle").val("");
        $("#surveyDescription").val("");
        $("#surveyIsActive").prop("checked", true);
        questionsContainer.empty();
        addQuestion();
    }

    function renderSurveyList(surveys) {
        const tbody = $("#surveyTableBody");
        tbody.empty();

        if (!surveys || surveys.length === 0) {
            tbody.append(
                "<tr><td colspan=\"5\" class=\"survey-empty\">등록된 설문이 없습니다.</td></tr>"
            );
            return;
        }

        surveys.forEach((survey) => {
            const activeLabel = survey.isActive === "Y" ? "활성" : "비활성";
            const createdAt = survey.createdAt ? new Date(survey.createdAt).toLocaleDateString() : "-";
            tbody.append(
                `<tr>
                    <td>${survey.surveyId}</td>
                    <td>${survey.title || "-"}</td>
                    <td>${survey.description || "-"}</td>
                    <td>${activeLabel}</td>
                    <td>${createdAt}</td>
                </tr>`
            );
        });
    }

    function loadSurveys() {
        $.getJSON(API_BASE)
            .done(function (data) {
                renderSurveyList(data);
            })
            .fail(function () {
                renderSurveyList([]);
            });
    }

    function addQuestion() {
        const index = questionsContainer.children().length + 1;
        const questionHtml = `
            <div class="survey-question" data-index="${index}">
                <div class="survey-question-title">
                    <strong>문항 ${index}</strong>
                    <button type="button" class="survey-btn-text remove-question">삭제</button>
                </div>
                <div class="survey-question-body">
                    <label>문항 내용</label>
                    <input type="text" class="survey-question-text" placeholder="문항 내용을 입력하세요" />

                    <label>문항 키</label>
                    <input type="text" class="survey-question-key" placeholder="예: Q${index}_KEY" />

                    <div class="survey-question-row">
                        <div>
                            <label>문항 유형</label>
                            <select class="survey-question-type">
                                <option value="SINGLE">단일 선택</option>
                                <option value="MULTI">복수 선택</option>
                                <option value="TEXT">주관식</option>
                            </select>
                        </div>
                        <div>
                            <label>복수 선택 최대</label>
                            <input type="number" class="survey-question-max" min="1" placeholder="예: 2" />
                        </div>
                        <div class="survey-question-required">
                            <label>
                                <input type="checkbox" class="survey-question-required-check" checked />
                                필수
                            </label>
                        </div>
                    </div>

                    <div class="survey-option-section">
                        <div class="survey-option-header">
                            <span>선택지</span>
                            <button type="button" class="survey-btn-secondary add-option">선택지 추가</button>
                        </div>
                        <div class="survey-options"></div>
                    </div>
                </div>
            </div>
        `;

        questionsContainer.append(questionHtml);
        const newQuestion = questionsContainer.children().last();
        newQuestion.find(".survey-question-max").prop("disabled", true);
        addOption(newQuestion);
    }

    function addOption(questionElement) {
        const optionsContainer = questionElement.find(".survey-options");
        const optionIndex = optionsContainer.children().length + 1;

        const optionHtml = `
            <div class="survey-option">
                <input type="text" class="survey-option-code" placeholder="코드 (예: A)" />
                <input type="text" class="survey-option-text" placeholder="선택지 텍스트" />
                <input type="text" class="survey-option-value" placeholder="값 (선택)" />
                <input type="number" class="survey-option-order" min="1" value="${optionIndex}" />
                <button type="button" class="survey-btn-text remove-option">삭제</button>
            </div>
        `;

        optionsContainer.append(optionHtml);
    }

    function normalizeQuestionOptions(question) {
        const options = [];
        question.find(".survey-option").each(function () {
            const option = $(this);
            options.push({
                optCode: option.find(".survey-option-code").val(),
                optText: option.find(".survey-option-text").val(),
                optValue: option.find(".survey-option-value").val(),
                optOrder: parseInt(option.find(".survey-option-order").val(), 10) || null,
                isActive: "Y"
            });
        });
        return options;
    }

    function buildSurveyPayload() {
        const payload = {
            title: $("#surveyTitle").val(),
            description: $("#surveyDescription").val(),
            isActive: $("#surveyIsActive").is(":checked") ? "Y" : "N",
            questions: []
        };

        questionsContainer.find(".survey-question").each(function (idx) {
            const question = $(this);
            const qType = question.find(".survey-question-type").val();
            const maxSelectValue = parseInt(question.find(".survey-question-max").val(), 10);

            payload.questions.push({
                qNo: idx + 1,
                qKey: question.find(".survey-question-key").val(),
                qText: question.find(".survey-question-text").val(),
                qType: qType,
                isRequired: question.find(".survey-question-required-check").is(":checked") ? "Y" : "N",
                maxSelect: qType === "MULTI" && !Number.isNaN(maxSelectValue) ? maxSelectValue : null,
                isActive: "Y",
                options: qType === "TEXT" ? [] : normalizeQuestionOptions(question)
            });
        });

        return payload;
    }

    $("#openSurveyModal").on("click", function () {
        console.log("[survey] 설문 등록 버튼 클릭");
        openModal();
    });

    $("#closeSurveyModal, #cancelSurveyModal").on("click", function () {
        closeModal();
    });

    modal.on("click", function (event) {
        if ($(event.target).is("#surveyModal")) {
            closeModal();
        }
    });

    $("#addSurveyQuestion").on("click", function () {
        addQuestion();
    });

    questionsContainer.on("click", ".remove-question", function () {
        $(this).closest(".survey-question").remove();
        questionsContainer.children().each(function (idx) {
            $(this).attr("data-index", idx + 1);
            $(this).find(".survey-question-title strong").text(`문항 ${idx + 1}`);
        });
    });

    questionsContainer.on("click", ".add-option", function () {
        const question = $(this).closest(".survey-question");
        addOption(question);
    });

    questionsContainer.on("click", ".remove-option", function () {
        $(this).closest(".survey-option").remove();
    });

    questionsContainer.on("change", ".survey-question-type", function () {
        const question = $(this).closest(".survey-question");
        const type = $(this).val();
        const optionSection = question.find(".survey-option-section");
        const maxSelectInput = question.find(".survey-question-max");

        if (type === "TEXT") {
            optionSection.hide();
            maxSelectInput.val("");
            maxSelectInput.prop("disabled", true);
        } else {
            optionSection.show();
            maxSelectInput.prop("disabled", type !== "MULTI");
        }
    });

    $("#submitSurvey").on("click", function () {
        const payload = buildSurveyPayload();
        console.log("[survey] 설문 등록 요청", payload);

        $.ajax({
            url: API_BASE,
            method: "POST",
            contentType: "application/json",
            data: JSON.stringify(payload)
        })
            .done(function () {
                closeModal();
                resetForm();
                loadSurveys();
            })
            .fail(function (xhr) {
                console.error("[survey] 설문 등록 실패", xhr);
                alert("설문 등록에 실패했습니다.");
            });
    });

    resetForm();
    loadSurveys();
});
