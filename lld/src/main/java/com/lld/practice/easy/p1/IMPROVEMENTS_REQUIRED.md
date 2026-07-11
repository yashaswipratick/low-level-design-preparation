# P1 Validation - Required Improvements (Revalidated)

Date: June 9, 2026
Path reviewed: `com/lld/practice/easy/p1`

## Revalidation Result
- Compile status: **PASSED**
- Command executed:

```bash
cd /Users/y0p03mn/preparation/src
javac com/lld/practice/easy/p1/model/*.java com/lld/practice/easy/p1/service/*.java com/lld/practice/easy/p1/service/impl/*.java
```

- First eight points status:
  - Point 1 (package/import mismatch): **RESOLVED**
  - Point 2 (`addBook` merge logic): **RESOLVED**
  - Point 3 (`returnBook` decrement for non-issued user): **RESOLVED**
  - Point 4 (`returnBook` issuedCopies underflow guard): **RESOLVED**
  - Point 5 (concurrency model): **RESOLVED**
  - Point 6 (search contract): **RESOLVED**
  - Point 7 (search null handling): **RESOLVED**
  - Point 8 (error/status contract): **RESOLVED**

## Findings (Updated)

### 1) [Resolved] Package and import mismatch
- Earlier blocker is no longer present.
- Current files now consistently use:
  - `package com.lld.practice.easy.p1.service`
  - `package com.lld.practice.easy.p1.service.impl`
  - `import com.lld.practice.easy.p1.model.*`
- Compile confirms class resolution is fixed.

### 2) [Resolved] `addBook` merge logic for variable incoming copies
- File: `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java:26`
- Current code:
  - `exisitingBook.setTotalCopies(exisitingBook.getTotalCopies() + book.getTotalCopies());`
- Revalidation:
  - Merge now uses incoming copies value instead of hardcoded `+1`.

### 3) [Resolved] `returnBook` no longer decrements for non-issued user
- File: `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java:90-95`
- Revalidation:
  - `issuedCopies--` now executes only when `bookToUser.get(bookId).contains(userId)` is true.
  - This fixes the earlier corruption path where non-issued users could trigger decrement.

### 3.1) [Resolved] Return contract for non-issued user is explicit
- File: `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java`
- Revalidation:
  - Non-issued return path now throws explicit typed error with status code `RETURN_NOT_ISSUED`.

### 4) [Resolved] Underflow guard on issued copies during return
- File: `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java`
- Revalidation:
  - Guard exists before decrement (`issuedCopies > 0`).
  - Underflow path now raises typed error with status code `DATA_CORRUPTION`.
  - Decrement is prevented when issued copies are already zero.

### 5) [Resolved] Concurrency model for issue/return
- File: `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java:13-20,50-77,80-108`
- Revalidation:
  - `bookToUser` switched to `ConcurrentHashMap`.
  - Added per-book lock registry (`bookLocks`) with `getBookLock(bookId)`.
  - `issueBook` and `returnBook` now run mutation logic inside `synchronized (getBookLock(bookId))`.
  - Issued-user set for each book uses `ConcurrentHashMap.newKeySet()`.
  - Compile remains passing after concurrency changes.

### 6) [Resolved] Search contract aligned with finalized design
- Files:
  - `com/lld/practice/easy/p1/service/LibrarySystem.java`
  - `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java`
- Revalidation:
  - Method signature is now `search(String query)`.
  - Matching logic uses exact case-insensitive `author OR title`.
  - `SearchContractTest` output: `Point-6 contract satisfied? true`.

### 7) [Resolved] Null handling in search
- File: `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java`
- Revalidation:
  - Null/empty/blank query is guarded and returns empty list without exception.
  - Guard now returns `Collections.emptyList()` (no console side effects).
  - `SearchNullHandlingTest` output:
    - `search(null) -> actual size: 0`
    - `search("") -> actual size: 0`

### 8) [Resolved] Structured error contract with status code + message
- Files:
  - `com/lld/practice/easy/p1/service/StatusCode.java`
  - `com/lld/practice/easy/p1/service/LibraryOperationException.java`
  - `com/lld/practice/easy/p1/service/impl/LibrarySystemImpl.java`
- Revalidation:
  - Replaced generic `RuntimeException` throws in service operations with `LibraryOperationException`.
  - Every failure now carries stable `StatusCode` + message.
  - `ErrorContractTest` output:
    - missing user -> `BOOK_OR_USER_NOT_FOUND`
    - duplicate issue -> `DUPLICATE_ISSUE`
    - return not issued -> `RETURN_NOT_ISSUED`
    - invalid input -> `INVALID_INPUT`
  - `Point-8 contract satisfied? true`.

## Revalidation Checklist (Next Pass)
1. All points (1 through 8) are resolved.
2. Keep compile + test run as regression checklist:
   - `SearchContractTest`
   - `SearchNullHandlingTest`
   - `ErrorContractTest`
   - `RaceConditionTest`
