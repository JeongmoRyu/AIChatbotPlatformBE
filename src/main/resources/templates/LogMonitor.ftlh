<!DOCTYPE html>
<html>
<head>
    <title>Dashboard</title>
    <style>
        #container {
            display: flex;
        }
        #logSection, #promptSection {
            width: 50%;
            padding: 10px;
        }
        .logEntry {
            width: 100%;
            height: 3em;
            overflow: auto;
            margin-bottom: 5px;
            resize: vertical;
        }
        #logOutput {
            width: 100%;
            height: 600px;
        }
        #prompt_1 {
            width: 100%;
            height: 500px;
        }
        #prompt_2 {
            width: 100%;
            height: 500px;
        }
    </style>
</head>
<body>
<div id="container">
    <div id="logSection">
        <#-- 날짜 형식을 "MM/dd HH:mm:ss"로 설정 -->
<#--        <#setting date_format="MM/dd HH:mm">-->
        <select id="recentListSelect">
            <#if recentLog?? && recentLog?has_content>
                <#list recentLog as item>
                    <#if item.seq?has_content>
                    <option id="${item.roomId}_${item.seq?replace(",", "")}">
                        <#if item.log?length gt 40>
<#--                            ${item.createdAt?string("MM/dd HH:mm")} ${item.log?substring(0, 40)}...-->
                            ${item.createdAt} ${item.log?substring(0, 40)}...
                        <#else>
<#--                            ${item.createdAt?string("MM/dd HH:mm")} ${item.log}-->
                            ${item.createdAt} ${item.log}
                        </#if>
                    </option>
                    </#if>
                </#list>
            </#if>
        </select>
        <input type="text" id="logDate" placeholder="날짜(2024/06/13)" value="${nowDate}" />
        <button onclick="changeRecentListSelect()">리스트 불러오기</button>
        <!--
        <input type="text" id="room_id" placeholder="Room ID" />
        <input type="text" id="seq" placeholder="Seq" />
        <button onclick="fetchLogs()">조회</button>
        -->
        <div id="logsContainer">
            <#if logs?? && logs?has_content>
                <#list logs as log>
                    <div class="logEntry">
                        <span>${log.diff/1000} 초</span>
                        <textarea readonly onclick="expandTextArea(this)">${log.log}</textarea>
                    </div>
                </#list>
            </#if>
        </div>
    </div>
<#--    <div id="promptSection">-->
<#--        <input type="hidden" id="chatbot_id" value="${chatbot_id!''}">-->
<#--        <button onclick="savePrompts()">저장</button>-->
<#--        <textarea id="prompt_1" placeholder="Prompt 1">${prompt_1!''}</textarea>-->
<#--        <textarea id="prompt_2" placeholder="Prompt 2">${prompt_2!''}</textarea>-->
<#--    </div>-->
</div>

<script>

    document.getElementById('recentListSelect').addEventListener('change', fetchLogs);

    function changeRecentListSelect() {
        var logDate = document.getElementById('logDate').value;
        const url = '/maum-admin/api/loglist?log_date=' + logDate;
        fetch(url)
            .then(response => response.json())
            .then(data => {
                const container = document.getElementById('recentListSelect');
                container.innerHTML = '';

                data.forEach(item => {
                        const roomId = item.room_id? item.room_id: "";
                        const seq = item.seq;
                        const createdAt = item.created_at;
                        const log = item.log;
                        const optionId = roomId + "_" + seq;
                        const title = item.title;

                        let optionText = createdAt + " " + log.substring(0, 40);
                        if (item.log && item.log.length > 40) {
                            optionText += "...";
                        }
                        const optionElement = document.createElement("option");
                        optionElement.id = optionId;
                        optionElement.textContent = optionText;

                        container.appendChild(optionElement);
                    }
                )

            })
    }

    function fetchLogs() {

        // select 요소에서 현재 선택된 option 요소를 가져옵니다.
        var selectElement = document.getElementById('recentListSelect');
        var selectedOptionElement = selectElement.options[selectElement.selectedIndex];

        // 선택된 option 요소의 id 값을 가져옵니다.
        var selectedOptionId = selectedOptionElement.id;


        // id 값을 '_' 기준으로 분리합니다.
        var parts = selectedOptionId.split('_');

        // 분리된 값들을 각각의 변수에 저장합니다.
        var roomId = parts[0];
        var seq = parts[1];

        try {
            roomId = Number(roomId.replace(/,/g,''));
            seq = Number(seq.replace(/,/g,''));
        } catch (e) {
            return;
        }

        // 결과를 확인하기 위한 로그 출력 (실제 사용 시에는 필요한 작업을 수행하세요.)
        console.log('roomId:', roomId);
        console.log('seq:', seq);

        // const id = document.getElementById('recentListSelect').value;
        // const roomId = document.getElementById('room_id').value;
        // const seq = document.getElementById('seq').value;
        // 서버로 roomId와 seq를 전송하여 로그 데이터를 조회
        // 예시 URL과 fetch 사용, 실제 구현에 맞게 수정 필요
        const url = '/maum-admin/api/logs?room_id=' + roomId + '&seq=' + seq;
        fetch(url)
            .then(response => response.json())
            .then(data => {
                const container = document.getElementById('logsContainer');
                container.innerHTML = '';

                const chatbot_id = data.chatbot_id;
                const prompt_1 = data.prompt_1;
                const prompt_2 = data.prompt_2;
                const totalTime = data.total_time;

                // document.getElementById("chatbot_id").value = chatbot_id;
                // document.getElementById("prompt_1").value = prompt_1;
                // document.getElementById("prompt_2").value = prompt_2;


                data.logs.forEach(log => {
                        const span = document.createElement('span');
                        // const date = new Date(log.created_at);
                        // const kstTime = new Date(date.getTime() + (9 * 60 * 60 * 1000));
                        span.textContent = log.title + ':' + (log.diff/1000) + '초';
                        container.appendChild(span);

                        const textarea = document.createElement('textarea');
                        textarea.classList.add('logEntry');
                        textarea.value = log.log;
                        textarea.onclick = function() { expandTextArea(this);};
                        container.appendChild(textarea);
                    }
                )

                const span = document.createElement('span');
                span.textContent = '총' + (totalTime/1000) + '초';
                container.appendChild(span);

                document.getElementById('logOutput').value = data;
            });
    }

    /*
    function savePrompts() {
        const prompt1 = document.getElementById('prompt_1').value;
        const prompt2 = document.getElementById('prompt_2').value;
        const chatbotId = document.getElementById('chatbot_id').value;
        // 서버로 prompt_1과 prompt_2를 전송하여 저장
        // 예시 URL과 fetch 사용, 실제 구현에 맞게 수정 필요
        fetch(`/maum-admin/api/save-prompts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt1, prompt2, chatbotId })
        })
            .then(response => response.json())
            .then(data => {
                // 저장 결과 처리
                alert(data.result);
                console.log('Save successful', data);
            });
    }
    */

    function expandTextArea(textarea) {
        if(textarea.style.height === '3em' || textarea.style.height === '') {
            textarea.style.height = textarea.scrollHeight + 'px';
        } else {
            textarea.style.height = '3em';
        }
    }

    // function populateRecentList() {
    //     // 예를 들어 서버에서 recentList를 가져오는 함수가 있다고 가정합니다.
    //     // const recentList = fetchRecentList(); // 이 함수는 서버로부터 recentList를 가져와야합니다.
    //
    //     // 셀렉트 박스의 DOM 요소를 가져옵니다.
    //     const selectBox = document.getElementById('recentListSelect');
    //
    //     // recentList의 각 요소에 대해 셀렉트 박스의 옵션을 생성합니다.
    //     recentList.forEach(item => {
    //         // 새로운 <option> 요소를 생성합니다.
    //         const option = document.createElement('option');
    //         // option의 value를 설정합니다.
    //         option.value = item.text;
    //         // option의 id(데이터 속성)를 설정합니다.
    //         option.dataset.id = item.id;
    //         // option의 텍스트를 설정합니다.
    //         option.textContent = item.text;
    //         // 생성된 옵션을 셀렉트 박스에 추가합니다.
    //         selectBox.appendChild(option);
    //     });
    // }

    fetchLogs();
</script>
</body>
</html>