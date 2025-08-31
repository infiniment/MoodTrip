document.addEventListener("DOMContentLoaded", function () {
  const wrapper = document.querySelector(".weather-recommand-region");
  const btn = document.querySelector(".search-btn");
  const icon = btn.querySelector("img");
  const heartImg = document.querySelector(".heart-img-wrapper img");
  const allSections = document.querySelectorAll(".section-content");
  const placeName = document.querySelector(".region-name span")?.textContent || "";

  // ✅ 1. 새로고침 시 섹션 초기화
  allSections.forEach(sec => sec.classList.remove("active"));

  // ✅ 2. 초기 슬라이드 닫기
  wrapper.classList.remove("expanded");
  wrapper.classList.add("collapsed");
  icon.src = "/static/image/recommand-tourist-attractions-detail/left-slider.png";

  // ✅ 3. 찜 상태 복원
  const savedLikes = JSON.parse(localStorage.getItem("likedPlaces") || "[]");
  const likedPlaces = new Set(savedLikes);

  const filledHeartURL = "https://cdn-icons-png.flaticon.com/512/833/833472.png";
  const emptyHeartURL = "https://cdn-icons-png.flaticon.com/512/1077/1077035.png";

  heartImg.src = likedPlaces.has(placeName) ? filledHeartURL : emptyHeartURL;

  // ✅ 4. 하트 토글 기능
  document.querySelector(".heart-img-wrapper").addEventListener("click", function () {
    if (likedPlaces.has(placeName)) {
      likedPlaces.delete(placeName);
      heartImg.src = emptyHeartURL;
      console.log(`${placeName} 찜 취소`);
    } else {
      likedPlaces.add(placeName);
      heartImg.src = filledHeartURL;
      console.log(`${placeName} 찜 완료`);
    }
    localStorage.setItem("likedPlaces", JSON.stringify([...likedPlaces]));
  });

  // ✅ 5. 슬라이더 토글
  btn.addEventListener("click", function () {
    const isExpanded = wrapper.classList.contains("expanded");
    wrapper.classList.toggle("expanded", !isExpanded);
    wrapper.classList.toggle("collapsed", isExpanded);
    icon.src = isExpanded
      ? "/static/image/recommand-tourist-attractions-detail/left-slider.png"
      : "/static/image/recommand-tourist-attractions-detail/right-slider.png";
  });

  // ✅ 6. 버튼 클릭 시 섹션 토글
  const regionButtons = document.querySelectorAll(".region-button");
  regionButtons.forEach(button => {
    button.addEventListener("click", function () {
      const target = button.dataset.target;

      // 대응하는 섹션 요소 찾기
      const targetSection = document.querySelector(
        `.section-content.${target === "detail"
          ? "place-detail-section"
          : target === "tag"
          ? "place-tag-info"
          : "map-placeholder"}`
      );

      const isActive = targetSection.classList.contains("active");

      // 전체 섹션 닫기
      allSections.forEach(sec => sec.classList.remove("active"));

      // 클릭한 게 기존에 닫혀있었다면 열기
      if (!isActive) {
        targetSection.classList.add("active");
      }
    });
  });

  // ✅ 7. 상세 정보 토글 버튼
  const toggleBtn = document.querySelector(".toggle-btn");
  const extraInfo = document.querySelector(".extra-info");
  const toggleText = document.querySelector(".toggle-text");
  const toggleIcon = document.querySelector(".toggle-icon");

  if (toggleBtn && extraInfo) {
    toggleBtn.addEventListener("click", () => {
      const isHidden = extraInfo.classList.contains("hidden");

      extraInfo.classList.toggle("hidden");
      toggleBtn.classList.toggle("active");

      if (isHidden) {
        toggleText.textContent = "내용 닫기";
        toggleIcon.textContent = "▲";
      } else {
        toggleText.textContent = "더 자세히 보기";
        toggleIcon.textContent = "▼";
      }
    });
  }
});
