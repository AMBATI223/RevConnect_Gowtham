document.addEventListener("DOMContentLoaded", function () {
    console.log("RevConnect App JS Loaded");

    // We use event delegation here to catch form submits and clicks on dynamically shown elements
    document.body.addEventListener("submit", function (e) {
        const form = e.target;
        if (form.tagName === "FORM") {
            const action = form.getAttribute("action");
            if (!action) return;

            // Handle Like Forms
            if (action.includes("/like")) {
                e.preventDefault();
                console.log("Liking post:", action);
                const btn = form.querySelector("button[type='submit']");
                const countSpan = btn.querySelector("span");

                if (!countSpan) {
                    console.error("Could not find count span in like button");
                }

                fetch(action, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    body: new URLSearchParams(new FormData(form))
                })
                    .then(response => {
                        if (!response.ok) {
                            if (response.status === 403 || response.status === 401) {
                                throw new Error('Please log in to like posts');
                            }
                            throw new Error('Action failed with status: ' + response.status);
                        }
                        return response.json();
                    })
                    .then(data => {
                        if (data.likesCount !== undefined) {
                            console.log("New likes count:", data.likesCount);
                            if (countSpan) countSpan.textContent = data.likesCount;
                            btn.classList.toggle('liked');
                            // Add a small scale animation
                            btn.style.transform = "scale(1.2)";
                            setTimeout(() => btn.style.transform = "scale(1)", 200);
                        }
                    })
                    .catch(err => {
                        console.error("Error toggling like:", err);
                        alert(err.message || "Unable to like post. Please check your connection or log in again.");
                    });
            }

            // Handle Comment Forms
            if (action.includes("/comment") && !action.includes("/delete")) {
                e.preventDefault();
                console.log("Commenting on post:", action);
                const input = form.querySelector("input[name='content']");
                if (!input || !input.value.trim()) return;

                // Extract postId strictly from action URL: /interactions/post/{id}/comment
                let postId = null;
                const match = action.match(/\/post\/(\d+)\/comment/);
                if (match) postId = match[1];

                if (!postId) {
                    console.error("Could not find post ID in action:", action);
                    // Fallback to data attribute if needed
                    const toggleBtn = form.closest('.post-card')?.querySelector('.toggle-comments-btn');
                    if (toggleBtn) postId = toggleBtn.getAttribute('data-post-id');
                }

                if (!postId) {
                    alert("System error: Could not identify post ID.");
                    return;
                }

                const commentsList = document.getElementById('commentsList-' + postId);
                const countSpan = document.getElementById('comment-count-' + postId);
                const submitBtn = form.querySelector("button[type='submit']");

                if (submitBtn) submitBtn.disabled = true;

                fetch(action, {
                    method: 'POST',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' },
                    body: new URLSearchParams(new FormData(form))
                })
                    .then(response => {
                        if (!response.ok) {
                            if (response.status === 403 || response.status === 401) {
                                throw new Error('Please log in to post comments');
                            }
                            throw new Error('Comment failed with status: ' + response.status);
                        }
                        return response.json();
                    })
                    .then(data => {
                        if (data.commentsCount !== undefined) {
                            console.log("New comment added. Count:", data.commentsCount);
                            if (countSpan) countSpan.textContent = data.commentsCount;

                            const commentHtml = `
                            <div class="inline-comment-item" style="opacity: 0; transform: translateY(10px); transition: all 0.3s;">
                                <img src="https://via.placeholder.com/32" class="inline-comment-avatar">
                                <div class="inline-comment-bubble">
                                    <span class="inline-comment-user fw-bold">${data.author}</span>
                                    <span class="inline-comment-text">${data.content}</span>
                                    <small class="inline-comment-date text-muted d-block" style="font-size: 0.7rem;">Just now</small>
                                </div>
                            </div>
                        `;
                            if (commentsList) {
                                commentsList.insertAdjacentHTML('afterbegin', commentHtml);
                                const firstChild = commentsList.firstElementChild;
                                setTimeout(() => {
                                    firstChild.style.opacity = "1";
                                    firstChild.style.transform = "translateY(0)";
                                }, 10);
                            }
                            input.value = "";
                        }
                    })
                    .catch(err => {
                        console.error("Error adding comment:", err);
                        alert(err.message || "Failed to post comment. Make sure you are logged in.");
                    })
                    .finally(() => {
                        if (submitBtn) submitBtn.disabled = false;
                    });
            }
        }
    });

    // Handle Comment Toggle Buttons via delegation
    document.body.addEventListener("click", function (e) {
        const btn = e.target.closest(".toggle-comments-btn");
        if (btn) {
            e.preventDefault();
            const postId = btn.getAttribute("data-post-id");
            console.log("Toggling comments for post:", postId);
            const commentFormArea = document.getElementById("commentForm-" + postId);
            const commentsList = document.getElementById("commentsList-" + postId);

            if (!commentFormArea || !commentsList) {
                console.error("Comment container elements not found for post:", postId);
                return;
            }

            if (commentFormArea.style.display === "none" || commentFormArea.style.display === "") {
                commentFormArea.style.display = "block";

                // Fetch existing comments if not loaded
                if (commentsList.innerHTML.trim() === "" || commentsList.innerHTML.includes('spinner')) {
                    commentsList.innerHTML = "<div class='text-center py-2'><div class='spinner-border spinner-border-sm text-primary'></div></div>";
                    fetch(`/interactions/post/${postId}/comments`)
                        .then(response => {
                            if (!response.ok) throw new Error('Failed to load comments');
                            return response.json();
                        })
                        .then(comments => {
                            if (comments.length === 0) {
                                commentsList.innerHTML = "<div class='text-muted small p-2 text-center'>No comments yet. Be the first!</div>";
                            } else {
                                commentsList.innerHTML = "";
                                comments.forEach(c => {
                                    const commentHtml = `
                                        <div class="inline-comment-item">
                                            <img src="${c.authorImage || 'https://via.placeholder.com/32'}" class="inline-comment-avatar">
                                            <div class="inline-comment-bubble">
                                                <a class="inline-comment-user fw-bold text-dark text-decoration-none" href="/users/profile/${c.author}">${c.author}</a>
                                                <span class="inline-comment-text">${c.content}</span>
                                                <small class="inline-comment-date text-muted d-block" style="font-size: 0.7rem;">${c.createdAt ? new Date(c.createdAt).toLocaleDateString([], { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }) : ''}</small>
                                            </div>
                                        </div>
                                    `;
                                    commentsList.insertAdjacentHTML('beforeend', commentHtml);
                                });
                            }
                        })
                        .catch(err => {
                            console.error("Error fetching comments:", err);
                            commentsList.innerHTML = "<div class='text-danger small p-2 text-center'>Failed to load comments</div>";
                        });
                }
            } else {
                commentFormArea.style.display = "none";
            }
        }
    });
});
