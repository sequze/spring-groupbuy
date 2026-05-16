document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("[data-comment-form]");
    if (!form) {
        return;
    }

    const contentField = form.querySelector("[name='content']");
    const submitButton = form.querySelector("[data-comment-submit]");
    const errorBox = form.querySelector("[data-comment-error]");
    const successBox = form.querySelector("[data-comment-success]");
    const commentsList = document.querySelector("[data-comments-list]");
    const emptyState = document.querySelector("[data-comments-empty]");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        hideFeedback();

        const content = contentField.value.trim();
        if (!content) {
            showError("Комментарий не может быть пустым.");
            contentField.focus();
            return;
        }

        setPending(true);

        try {
            const response = await fetch(form.action, {
                method: "POST",
                headers: buildHeaders(),
                body: JSON.stringify({
                    purchaseItemId: Number(form.dataset.purchaseItemId),
                    content
                })
            });

            if (!response.ok) {
                throw new Error(await readErrorMessage(response));
            }

            const comment = await response.json();
            commentsList.prepend(renderComment(comment));
            updateEmptyState();
            contentField.value = "";
            showSuccess("Комментарий добавлен.");
        } catch (error) {
            showError(error.message || "Не удалось добавить комментарий. Попробуйте еще раз.");
        } finally {
            setPending(false);
        }
    });

    commentsList.addEventListener("click", async (event) => {
        const editButton = event.target.closest("[data-comment-edit]");
        const cancelButton = event.target.closest("[data-comment-cancel]");
        const deleteButton = event.target.closest("[data-comment-delete]");

        if (editButton) {
            showEditForm(editButton.closest("[data-comment-id]"));
            return;
        }

        if (cancelButton) {
            hideEditForm(cancelButton.closest("[data-comment-id]"), true);
            return;
        }

        if (deleteButton) {
            await deleteComment(deleteButton.closest("[data-comment-id]"));
        }
    });

    commentsList.addEventListener("submit", async (event) => {
        const editForm = event.target.closest("[data-comment-edit-form]");
        if (!editForm) {
            return;
        }

        event.preventDefault();
        await updateComment(editForm.closest("[data-comment-id]"));
    });

    function buildHeaders() {
        const headers = {
            "Accept": "application/json",
            "Content-Type": "application/json"
        };
        const csrfToken = document.querySelector("meta[name='_csrf']")?.content;
        const csrfHeader = document.querySelector("meta[name='_csrf_header']")?.content;
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        return headers;
    }

    async function readErrorMessage(response) {
        try {
            const payload = await response.json();
            return payload.message || "Не удалось выполнить действие. Попробуйте еще раз.";
        } catch (error) {
            return "Не удалось выполнить действие. Попробуйте еще раз.";
        }
    }

    function renderComment(comment) {
        const card = document.createElement("article");
        card.className = "entity-card comment-card";
        card.dataset.commentId = comment.id;
        card.dataset.canEdit = String(Boolean(comment.canEdit));
        card.dataset.canDelete = String(Boolean(comment.canDelete));

        const head = document.createElement("div");
        head.className = "entity-head";

        const contentColumn = document.createElement("div");
        contentColumn.className = "comment-content-column";

        const body = document.createElement("p");
        body.className = "comment-body";
        body.dataset.commentBody = "";
        body.textContent = comment.content;

        const meta = document.createElement("div");
        meta.className = "muted comment-meta";
        meta.textContent = `Автор: ${comment.authorDisplayName}`;

        contentColumn.append(body, meta, renderEditForm(comment.content));

        const side = document.createElement("div");
        side.className = "comment-side";
        side.append(renderBadges(comment));

        const actions = renderCommentActions(comment);
        if (actions) {
            side.append(actions);
        }

        head.append(contentColumn, side);
        card.append(head);
        return card;
    }

    function renderEditForm(content) {
        const editForm = document.createElement("form");
        editForm.className = "comment-edit-form";
        editForm.dataset.commentEditForm = "";
        editForm.hidden = true;

        const textarea = document.createElement("textarea");
        textarea.name = "content";
        textarea.maxLength = 2000;
        textarea.required = true;
        textarea.dataset.commentEditContent = "";
        textarea.value = content;

        const actions = document.createElement("div");
        actions.className = "comment-actions";

        const saveButton = document.createElement("button");
        saveButton.className = "button";
        saveButton.type = "submit";
        saveButton.textContent = "Сохранить";

        const cancelButton = document.createElement("button");
        cancelButton.className = "button-secondary";
        cancelButton.type = "button";
        cancelButton.dataset.commentCancel = "";
        cancelButton.textContent = "Отмена";

        actions.append(saveButton, cancelButton);
        editForm.append(textarea, actions);
        return editForm;
    }

    function renderBadges(comment) {
        const badges = document.createElement("div");
        badges.className = "badge-row";

        const idBadge = document.createElement("span");
        idBadge.className = "badge";
        idBadge.textContent = `ID ${comment.id}`;
        badges.append(idBadge);

        const editedBadge = document.createElement("span");
        editedBadge.className = "badge badge-success";
        editedBadge.dataset.commentEdited = "";
        editedBadge.textContent = "Отредактировано";
        editedBadge.hidden = !comment.edited;
        badges.append(editedBadge);

        return badges;
    }

    function renderCommentActions(comment) {
        if (!comment.canEdit && !comment.canDelete) {
            return null;
        }

        const actions = document.createElement("div");
        actions.className = "comment-actions";

        if (comment.canEdit) {
            const editButton = document.createElement("button");
            editButton.className = "icon-button";
            editButton.type = "button";
            editButton.dataset.commentEdit = "";
            editButton.setAttribute("aria-label", "Редактировать");
            editButton.title = "Редактировать";
            editButton.append(createIcon([
                "M4 20h4l10.5-10.5a2.1 2.1 0 0 0-4-4L4 16v4Z",
                "m13.5 6.5 4 4"
            ]));
            actions.append(editButton);
        }

        if (comment.canDelete) {
            const deleteButton = document.createElement("button");
            deleteButton.className = "icon-button icon-button-danger";
            deleteButton.type = "button";
            deleteButton.dataset.commentDelete = "";
            deleteButton.setAttribute("aria-label", "Удалить");
            deleteButton.title = "Удалить";
            deleteButton.append(createIcon([
                "M4 7h16",
                "M10 11v6",
                "M14 11v6",
                "M6 7l1 13h10l1-13",
                "M9 7V4h6v3"
            ]));
            actions.append(deleteButton);
        }

        return actions;
    }

    function createIcon(paths) {
        const svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute("viewBox", "0 0 24 24");
        svg.setAttribute("aria-hidden", "true");

        paths.forEach((pathData) => {
            const path = document.createElementNS("http://www.w3.org/2000/svg", "path");
            path.setAttribute("d", pathData);
            svg.append(path);
        });

        return svg;
    }

    function showEditForm(card) {
        hideFeedback();
        const body = card.querySelector("[data-comment-body]");
        const editForm = card.querySelector("[data-comment-edit-form]");
        const textarea = card.querySelector("[data-comment-edit-content]");
        textarea.value = body.textContent;
        body.hidden = true;
        editForm.hidden = false;
        textarea.focus();
    }

    function hideEditForm(card, resetValue) {
        const body = card.querySelector("[data-comment-body]");
        const editForm = card.querySelector("[data-comment-edit-form]");
        const textarea = card.querySelector("[data-comment-edit-content]");
        if (resetValue) {
            textarea.value = body.textContent;
        }
        editForm.hidden = true;
        body.hidden = false;
    }

    async function updateComment(card) {
        hideFeedback();

        const content = card.querySelector("[data-comment-edit-content]").value.trim();
        if (!content) {
            showError("Комментарий не может быть пустым.");
            return;
        }

        setCommentPending(card, true);

        try {
            const response = await fetch(`/api/comments/${card.dataset.commentId}`, {
                method: "PUT",
                headers: buildHeaders(),
                body: JSON.stringify({content})
            });

            if (!response.ok) {
                throw new Error(await readErrorMessage(response));
            }

            const comment = await response.json();
            card.querySelector("[data-comment-body]").textContent = comment.content;
            const editedBadge = card.querySelector("[data-comment-edited]");
            if (editedBadge) {
                editedBadge.hidden = !comment.edited;
            }
            hideEditForm(card, false);
            showSuccess("Комментарий обновлен.");
        } catch (error) {
            showError(error.message || "Не удалось обновить комментарий. Попробуйте еще раз.");
        } finally {
            setCommentPending(card, false);
        }
    }

    async function deleteComment(card) {
        hideFeedback();

        if (!confirm("Удалить комментарий?")) {
            return;
        }

        setCommentPending(card, true);

        try {
            const response = await fetch(`/api/comments/${card.dataset.commentId}`, {
                method: "DELETE",
                headers: buildHeaders()
            });

            if (!response.ok) {
                throw new Error(await readErrorMessage(response));
            }

            card.remove();
            updateEmptyState();
            showSuccess("Комментарий удален.");
        } catch (error) {
            showError(error.message || "Не удалось удалить комментарий. Попробуйте еще раз.");
            setCommentPending(card, false);
        }
    }

    function setPending(isPending) {
        submitButton.disabled = isPending;
        submitButton.textContent = isPending ? "Добавляем..." : "Добавить комментарий";
    }

    function setCommentPending(card, isPending) {
        card.querySelectorAll("button, textarea").forEach((control) => {
            control.disabled = isPending;
        });
    }

    function updateEmptyState() {
        if (emptyState) {
            emptyState.hidden = Boolean(commentsList.querySelector("[data-comment-id]"));
        }
    }

    function hideFeedback() {
        errorBox.hidden = true;
        successBox.hidden = true;
        errorBox.textContent = "";
        successBox.textContent = "";
    }

    function showError(message) {
        errorBox.textContent = message;
        errorBox.hidden = false;
    }

    function showSuccess(message) {
        successBox.textContent = message;
        successBox.hidden = false;
    }
});
