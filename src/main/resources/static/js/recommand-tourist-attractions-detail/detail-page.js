document.addEventListener("DOMContentLoaded", function () {
  const currentUser = {
    name: "김치국밥",
    isLoggedIn: true
  };

  const reviewDB = [];
  const reviewContainer = document.getElementById("reviewContainer");
  const reviewCountEl = document.getElementById("reviewcount");
  const ratingInput = document.getElementById("ratingValue");
  const stars = document.querySelectorAll(".star");
  const starContainer = document.getElementById("starRating");
  const moreBtn = document.querySelector(".review-more");

  let showingAll = false;

 function maskUsername(name) {
  const len = name.length;
  const visible = Math.ceil(len / 2);
  return name.slice(0, visible) + "*".repeat(len - visible);
}


  function formatDate(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
  }

  function updateReviewCount() {
    if (reviewCountEl) {
      reviewCountEl.textContent = `총 ${reviewDB.length}개의 후기`;
    }
  }

  function renderReviews(limit = 3) {
  reviewContainer.innerHTML = "";
  const sorted = [...reviewDB].sort((a, b) => b.timestamp - a.timestamp);
  const sliced = sorted.slice(0, limit);

  sliced.forEach(r => {
    const li = document.createElement("li");
    li.innerHTML = `
      <div class="review-box">
        <strong>${maskUsername(r.username)}</strong> <span class="star-icon">⭐</span> ${r.rating || 0}점
        <div class="review-text">${r.review}</div>
        <div class="review-date">🕒 ${formatDate(r.timestamp)}</div>
      </div>
    `;
    reviewContainer.appendChild(li);
  });

  updateReviewCount();

  // 더보기 버튼 토글 여부
  if (reviewDB.length <= 3) {
    moreBtn.style.display = "none";
  } else {
    moreBtn.style.display = "inline-block";
    moreBtn.textContent = showingAll ? "접기 ❮" : "더보기 ❯";
  }
}


  function updateStars(score) {
    stars.forEach((star, index) => {
      star.classList.remove("full", "half", "empty");
      const i = index + 1;
      if (score >= i) {
        star.classList.add("full");
      } else if (score >= i - 0.5) {
        star.classList.add("half");
      } else {
        star.classList.add("empty");
      }
    });
  }

  stars.forEach((star, index) => {
    star.addEventListener("click", (e) => {
      const rect = star.getBoundingClientRect();
      const clickX = e.clientX - rect.left;
      const isHalf = clickX <= rect.width / 2;
      const value = index + (isHalf ? 0.5 : 1);
      ratingInput.value = value;
      updateStars(value);

      starContainer.classList.add("active");
      setTimeout(() => starContainer.classList.remove("active"), 150);
    });
  });

  updateStars(0);

  document.querySelector(".review-form").addEventListener("submit", function (e) {
    e.preventDefault();

    const input = document.getElementById("review");
    const reviewText = input.value.trim();
    const rating = parseFloat(ratingInput.value);

    if (!currentUser.isLoggedIn) {
      alert("로그인 후 작성 가능합니다.");
      return;
    }

    if (!reviewText) {
      alert("후기를 작성해주세요.");
      input.focus();
      return;
    }

    if (reviewText.length > 500) {
      alert("후기는 500자 이하로 작성해주세요.");
      return;
    }

    if (isNaN(rating) || rating <= 0) {
      alert("별점을 선택해주세요.");
      return;
    }

    const maskedName = maskUsername(currentUser.name);

    reviewDB.push({
      username: maskedName,
      review: reviewText,
      rating,
      timestamp: new Date()
    });

    input.value = "";
    ratingInput.value = 0;
    updateStars(0);
    showingAll = false;
    renderReviews(3);
  });

  if (moreBtn) {
    moreBtn.addEventListener("click", () => {
      showingAll = !showingAll;
      renderReviews(showingAll ? 99 : 3);
    });
  }

  // 테스트용 더미 데이터
  reviewDB.push(
    {
      username: "서유진",
      review: "방문 추천드려요!",
      rating: 4,
      timestamp: new Date("2024-08-22")
    },
    {
      username: "마라탕개맛있다",
      review: "생각보다 괜찮았어요",
      rating: 3.5,
      timestamp: new Date("2024-12-10")
    }
  );

  renderReviews(3);
});
