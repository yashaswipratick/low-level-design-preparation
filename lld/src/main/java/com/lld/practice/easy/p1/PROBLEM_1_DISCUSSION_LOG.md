# LLD Problem 1: Library Book Catalog — Interview Discussion Log

**Date Started:** June 4, 2026  
**Problem:** Design a system to add books, search by title/author, and mark copies as available or issued.

---

## Initial Approach (User's First Submission)

```
class Book
  String id
  String title
  String author
  int totalCopies
  int issuedCopies

class User
  String id
  String name
  String email
  String mobileNo
  List<Book> issuedBooks

class LibrarySystem
  Map<String, Book> bookCatalog
  Map<String, User> userCatalog
  Map<String, String> issuedBooks

Methods:
  search(String query) -> List<Book>
  addBook(Book book) -> void
  issueBook(String bookId, String userId) -> void
  returnBook(String bookId, String userId) -> void
```

---

## Interviewer Questions & User Corrections (Round 1)

### Q1: Name vs Title fields
**Interviewer:** What's the difference between `name` and `title`?  
**User's Correction:** They are the same. Remove `name`, keep only `title`.

### Q2: Multiple copies modeling
**Interviewer:** How will you represent multiple copies where one is issued and another is available?  
**User's Decision:** Two fields: `totalCopies` and `issuedCopies`.

### Q3-Q10: Logic Issues (First Pass)

#### Q3: issueBook logic error
**Interviewer:** In `issueBook`, why is `issuedCopies` being decremented?  
**User's Correction:** Should be **incremented by 1**, not decremented.  
**Logic:** `issuedCopies++` when a copy is issued.

#### Q4: returnBook logic error
**Interviewer:** Same for `returnBook`?  
**User's Correction:** Should be **decremented by 1**, not incremented.  
**Logic:** `issuedCopies--` when a copy is returned.

#### Q5: returnBook boundary check
**Interviewer:** Your condition `if(totalCopies > issuedCopies)` — what if `issuedCopies == 0`?  
**User's Correction:** Should check `if(issuedCopies > 0)` before decrement.

#### Q6: issuedBooks map structure
**Interviewer:** `Map<String, String> issuedBooks` — what are key/value? How track multiple copies to different users?  
**User's Correction:** Should be `Map<String, Set<UserID>>` — book ID maps to set of user IDs.

#### Q7: User validation
**Interviewer:** Where do you validate `userId` exists?  
**User's Correction:** Need to add validation before issue/return.

#### Q8: Duplicate issue prevention
**Interviewer:** How prevent same user issuing same book twice?  
**User's Correction:** Need to add this check.

#### Q9: Dual tracking issue
**Interviewer:** `User.issuedBooks` and system-level `issuedBooks` map both track — which is source of truth?  
**User's Correction:** Need to keep them in sync; must update both.

#### Q10: Search logic — exact vs partial
**Interviewer:** Should search be regex/contains or exact match?  
**User's Correction:** Use exact `equalsIgnoreCase` match (not partial).

#### Q11: addBook duplicate handling
**Interviewer:** What if same book ID added twice?  
**User's Correction:** Should **merge** — increment `totalCopies` by the new amount, keep `issuedCopies` as is.

#### Q12: Method return types
**Interviewer:** Return `void + print` or error object?  
**User's Correction:** Should return **error object** (not void + println).

---

## Round 2: Deeper Clarifications

### Q1: Same author, different books
**Interviewer:** If "Harry Potter 1" and "Harry Potter 2" are both by J.K. Rowling with different book IDs, can user issue both?  
**User's Clarification:** Different book IDs → can issue both. Only if **same author and same book ID** should it fail.  
**Rule:** One user can have **at most one book per author**.

### Q2: Error object fields
**Interviewer:** Message only, or include status code?  
**User's Clarification:** Should include **status code** (e.g., `INSUFFICIENT_COPIES`, `USER_NOT_FOUND`).

### Q3: User fetch responsibility
**Interviewer:** Who fetches `User` from `userCatalog`?  
**User's Clarification:** **Method should fetch it internally** from `userCatalog` (not caller).  
**Decision:** Pass `User` object instead of `userId` to method.

### Q4: Search partial match
**Interviewer:** Should `"harry"` match `"Harry Potter and the Goblet of Fire"`?  
**User's Clarification:** No — use **exact match only**, case-insensitive.

### Q5: Remove book from User.issuedBooks
**Interviewer:** How identify which Book object to remove from Set?  
**User's Clarification:** Compare by **book.id** (not object reference).

### Q6: Multiple error codes
**Interviewer:** Different errors for different failure types?  
**User's Clarification:** **Yes** — each error type has its own status code and message.

### Q7: Concurrency
**Interviewer:** Race conditions on concurrent issue/return?  
**User's Clarification:** **Yes, need synchronization** — use `ConcurrentHashMap` or `synchronized` blocks.

---

## Round 3: Implementation Details (Current)

### Q1: Synchronization strategy
**Interviewer:** Use method-level `synchronized` or per-book locking?  
**User's Response:** Pending (not yet decided).

### Q2: Search — exact match confirmation
**Interviewer:** Exact match or substring?  
**User's Unclear Response:** Says "exactly the query is passed" — needs clarification.  
**Status:** **NEED TO CLARIFY** tomorrow.

### Q3: Error priority
**Interviewer:** If both `USER_NOT_FOUND` and `INSUFFICIENT_COPIES` occur, which error first?  
**User's Response:** "Need to think on priority" — **PENDING**.

### Q4: addBook duplicate merge
**Interviewer:** Confirm merge logic with totalCopies increment.  
**User's Clarification:** **Yes, merge** — increment `totalCopies`, reset `issuedCopies` to 0 or keep existing.  
**Status:** Keep **existing** `issuedCopies` (do not reset).

### Q5: Return logic with Set
**Interviewer:** If U1 and U2 both issue Book B1, then U1 returns:
- Should Set become {U2}
- How code distinguishes U1 from U2?

**User's Response:** "Need to think on this" — **PENDING**.

### Q6: Dual tracking redundancy
**Interviewer:** Both `User.issuedBooks` and `LibrarySystem.bookToUsers` map — is one redundant?  
**User's Response:** "Need to think on this" — **PENDING**.

### Q7: Search output
**Interviewer:** Include availability count or just metadata?  
**User's Clarification:** **Just metadata** (no availability info in search result).

---

## Pending Decisions (Resume Tomorrow)

1. **Synchronization strategy** — Method-level vs per-book locking?
2. **Search clarification** — Exact match OR contains/partial?
3. **Error priority** — Which error returned if multiple failures?
4. **Set removal logic** — How ensure only correct user removed from Set when both U1 and U2 have issued same book?
5. **Dual tracking** — Is `User.issuedBooks` redundant given `LibrarySystem.bookToUsers`?

---

## Finalized Decisions (June 9, 2026)

1. **Synchronization strategy:** Per-book locking (lock by `bookId`), not whole-system lock.
2. **Search behavior:** Exact match only, case-insensitive (`equalsIgnoreCase`).
3. **Error priority in `issueBook(...)`:** Top-to-bottom validation order.
4. **Set removal in return flow:** Remove only given `userId` from `bookToUsers[bookId]`; if set becomes empty, remove the `bookId` key.
5. **Source of truth:** Keep only one issuance tracking source: `bookToUsers` (remove `User.issuedBooks` tracking to avoid redundancy).

---

## Final Business Logic Pseudocode (Locked)

### `addBook(book)`
1. Validate `book != null`, `book.id/title/author` non-empty, `totalCopies > 0`.
2. If `bookCatalog` does not contain `book.id`, insert new `Book` with `issuedCopies = 0`.
3. Else merge inventory:
   - `existing.totalCopies += book.totalCopies`
   - keep `existing.issuedCopies` unchanged.
4. Return success response.

### `search(query)`
1. Validate non-empty query.
2. Iterate all books in `bookCatalog`.
3. If `book.title.equalsIgnoreCase(query)` OR `book.author.equalsIgnoreCase(query)`, add to result.
4. Return `List<Book>` metadata only.

### `issueBook(bookId, userId)`
1. Validate in this order (top-to-bottom):
   - if user missing -> `USER_NOT_FOUND`
   - if book missing -> `BOOK_NOT_FOUND`
2. Acquire lock for `bookId`.
3. Re-fetch book under lock.
4. Duplicate rule check (same user already has this `bookId` in `bookToUsers[bookId]`) -> `DUPLICATE_ISSUE_RULE`.
5. Check inventory: `available = totalCopies - issuedCopies`; if `available <= 0` -> `INSUFFICIENT_COPIES`.
6. Update state:
   - `book.issuedCopies++`
   - `bookToUsers.computeIfAbsent(bookId, k -> new HashSet<>()).add(userId)`
7. Release lock.
8. Return success response.

### `returnBook(bookId, userId)`
1. Validate in this order (top-to-bottom):
   - if user missing -> `USER_NOT_FOUND`
   - if book missing -> `BOOK_NOT_FOUND`
2. Acquire lock for `bookId`.
3. Re-fetch book and issued-user set under lock.
4. If issued-user set missing or does not contain `userId` -> `RETURN_NOT_ALLOWED`.
5. If `book.issuedCopies <= 0` -> `INVENTORY_MISMATCH`.
6. Update state:
   - `book.issuedCopies--`
   - remove `userId` from `bookToUsers[bookId]`
   - if set empty, remove `bookId` key from map
7. Release lock.
8. Return success response.

### Invariants
- `0 <= issuedCopies <= totalCopies`
- `bookToUsers[bookId].size() <= issuedCopies`
- All issue/return mutations for same `bookId` happen under same per-book lock.

---

## Current Design Summary

### Classes
- `Book` (id, title, author, totalCopies, issuedCopies)
- `User` (id, name, email, mobileNo, Set<Book> issuedBooks)
- `LibrarySystem` (bookCatalog, userCatalog, bookToUsers Map)

### Key Methods
- `search(query)` — exact case-insensitive match on title/author
- `addBook(book)` — merge if ID exists, increment totalCopies
- `issueBook(bookId, user)` — increment issuedCopies, add to User.issuedBooks, add userId to bookToUsers set
- `returnBook(bookId, user)` — decrement issuedCopies, remove from User.issuedBooks, remove userId from bookToUsers set

### Data Structures
- `Map<String, Book> bookCatalog`
- `Map<String, User> userCatalog`
- `Map<String, Set<String>> bookToUsers` (book ID → set of user IDs)
- Use `ConcurrentHashMap` for thread safety

### Error Handling
- Return Error object with statusCode and message
- Status codes: `INSUFFICIENT_COPIES`, `USER_NOT_FOUND`, `AUTHOR_DUPLICATE`, `BOOK_NOT_FOUND`

---

## Next Steps (Tomorrow)

1. Clarify exact vs partial search
2. Decide on error priority rules
3. Design Set removal logic for multi-user scenarios
4. Document synchronization approach
5. Finalize pseudocode with complete business logic

## June 9, 2026
## Coding Summary (Locked Scope)

- Problem scope: add books, exact-search by title/author, issue copy, return copy.
- `Book` is title-level aggregate: `id`, `title`, `author`, `totalCopies`, `issuedCopies`.
- User existence is validated from `userCatalog` in service methods.
- Issuance source of truth is only `bookToUsers: Map<String, Set<String>>`.
- Search behavior is exact match, case-insensitive, metadata-only response.
- Duplicate add by same `bookId` merges inventory by incrementing `totalCopies` and preserving `issuedCopies`.
- Synchronization is per-book locking using `bookId` lock scope.
- Error handling is structured (status code + message), no `println`-style flow control.

## Final Execution Steps (For Implementation)

1. Define domain models and fields:
   - `Book(id, title, author, totalCopies, issuedCopies)`
   - `User(id, name, email, mobileNo)`
2. Define storage in `LibrarySystem`:
   - `bookCatalog: Map<String, Book>`
   - `userCatalog: Map<String, User>`
   - `bookToUsers: Map<String, Set<String>>`
3. Implement `addBook(book)`:
   - Validate input
   - Insert new book if absent
   - Else merge by increasing `totalCopies`
4. Implement `search(query)`:
   - Validate query
   - Iterate `bookCatalog`
   - Match with `equalsIgnoreCase` on title or author
   - Return matched book metadata list
5. Implement `issueBook(bookId, userId)` with priority order:
   - `USER_NOT_FOUND` -> `BOOK_NOT_FOUND`
   - Lock by `bookId`
   - Duplicate issue check for same `bookId` + `userId`
   - Availability check from `totalCopies - issuedCopies`
   - Mutate: `issuedCopies++`, add userId into `bookToUsers[bookId]`
6. Implement `returnBook(bookId, userId)` with priority order:
   - `USER_NOT_FOUND` -> `BOOK_NOT_FOUND`
   - Lock by `bookId`
   - Verify user exists in issued set for that `bookId`
   - Verify `issuedCopies > 0`
   - Mutate: `issuedCopies--`, remove `userId` from set, remove key if set becomes empty
7. Enforce invariants after mutations:
   - `0 <= issuedCopies <= totalCopies`
   - `bookToUsers[bookId].size() <= issuedCopies`
8. Keep response contract uniform:
   - Success or failure via structured response object (status code + message).

