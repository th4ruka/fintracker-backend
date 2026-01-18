# Frontend Development Plan - Personal Finance Dashboard

## Overview
This document outlines the complete development plan for building a Next.js 14 frontend application for the Personal Finance Tracker backend API. The plan is organized into logical phases with specific, actionable tasks.

## Tech Stack

### Core Framework
- **Next.js 14+** with App Router
- **TypeScript** for type safety
- **React 18+** with Server Components

### UI & Styling
- **Tailwind CSS** for styling
- **shadcn/ui** for component library (built on Radix UI)
- **Lucide React** for icons (comes with shadcn/ui)

### Data Management
- **TanStack Query (React Query v5)** for server state management
- **Axios** or native **Fetch API** for HTTP requests
- **Zustand** (optional, minimal use) for client state if needed

### Forms & Validation
- **React Hook Form** for form management
- **Zod** for schema validation
- Integration with shadcn/ui form components

### Data Visualization
- **Recharts** for financial charts and graphs
- **TanStack Table** for advanced data tables

### Utilities
- **date-fns** for date manipulation and formatting
- **clsx** and **tailwind-merge** for conditional styling

### Development Tools
- **ESLint** for code linting
- **Prettier** for code formatting
- **Vitest** for unit testing
- **React Testing Library** for component testing

---

## Development Phases

### Phase 1: Project Foundation (Tasks 1-9)
**Goal:** Set up the project infrastructure and configuration

#### Tasks:
1. Initialize Next.js 14 project with TypeScript
   ```bash
   npx create-next-app@latest fin-tracker-frontend --typescript --tailwind --app --src-dir
   ```

2. Configure Tailwind CSS and install shadcn/ui
   ```bash
   npx shadcn-ui@latest init
   ```

3. Set up project folder structure:
   ```
   src/
   ├── app/              # Next.js app router pages
   ├── components/       # React components
   │   ├── ui/          # shadcn/ui components
   │   ├── forms/       # Form components
   │   ├── charts/      # Chart components
   │   └── layouts/     # Layout components
   ├── lib/             # Utilities and helpers
   │   ├── api/        # API client and services
   │   └── utils/      # Utility functions
   ├── hooks/           # Custom React hooks
   ├── types/           # TypeScript type definitions
   └── constants/       # Application constants
   ```

4. Install core dependencies
   ```bash
   npm install @tanstack/react-query axios react-hook-form zod @hookform/resolvers date-fns recharts @tanstack/react-table
   ```

5. Configure environment variables
   - Create `.env.local` with `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`
   - Create `.env.example` for documentation

**Deliverable:** Fully configured Next.js project ready for development

---

### Phase 2: Type Definitions & API Layer (Tasks 10-15)
**Goal:** Create TypeScript types matching backend DTOs and build API client infrastructure

#### Backend API Endpoints:
- Users: `/api/v1/users`
- Accounts: `/api/accounts`
- Financial Records: `/api/records`
- Categories: `/api/categories`
- Labels: `/api/labels`
- Templates: `/api/templates`

#### Type Definitions Required:

**src/types/user.ts:**
```typescript
export interface User {
  id: number;
  name: string;
  email: string;
  dob: string; // ISO date string
  age: number;
}

export interface UserCreateRequest {
  name: string;
  email: string;
  dob: string;
}

export interface UserUpdateRequest {
  name?: string;
  email?: string;
  dob?: string;
}
```

**src/types/account.ts:**
```typescript
export enum AccountType {
  GENERAL = 'GENERAL',
  CREDIT = 'CREDIT',
  OVERDRAFT = 'OVERDRAFT'
}

export interface Account {
  id: number;
  userId: number;
  accountName: string;
  accountType: AccountType;
  balance: number;
  creditLimit?: number;
  overdraftLimit?: number;
}

export interface AccountCreateRequest {
  userId: number;
  accountName: string;
  accountType: AccountType;
  initialBalance: number;
  creditLimit?: number;
  overdraftLimit?: number;
}

export interface AccountUpdateRequest {
  accountName?: string;
  creditLimit?: number;
  overdraftLimit?: number;
}
```

**src/types/financial-record.ts:**
```typescript
export enum RecordType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
  TRANSFER = 'TRANSFER'
}

export interface FinancialRecord {
  id: number;
  userId: number;
  accountId: number;
  categoryId: number;
  recordType: RecordType;
  amount: number;
  recordDate: string; // ISO date
  description?: string;
  toAccountId?: number;
  labels: Label[];
}

export interface FinancialRecordCreateRequest {
  userId: number;
  accountId: number;
  categoryId: number;
  recordType: RecordType;
  amount: number;
  recordDate: string;
  description?: string;
  toAccountId?: number;
  labelIds?: number[];
}
```

**src/types/category.ts:**
```typescript
export enum CategoryType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE'
}

export interface Category {
  id: number;
  categoryName: string;
  categoryType: CategoryType;
  userId?: number; // null for system defaults
}

export interface CategoryCreateRequest {
  categoryName: string;
  categoryType: CategoryType;
  userId?: number;
}
```

**src/types/label.ts:**
```typescript
export interface Label {
  id: number;
  labelName: string;
  color: string;
  userId: number;
}

export interface LabelCreateRequest {
  labelName: string;
  color: string;
  userId: number;
}
```

**src/types/template.ts:**
```typescript
export interface Template {
  id: number;
  templateName: string;
  userId: number;
  categoryId: number;
  recordType: RecordType;
  defaultAmount?: number;
  description?: string;
}

export interface TemplateCreateRequest {
  templateName: string;
  userId: number;
  categoryId: number;
  recordType: RecordType;
  defaultAmount?: number;
  description?: string;
}
```

#### API Client Structure:

**src/lib/api/client.ts:**
- Base Axios instance with interceptors
- Error handling
- Request/response transformations

**src/lib/api/services/users.ts:**
- `getUsers()`, `getUserById(id)`, `createUser(data)`, `updateUser(id, data)`, `deleteUser(id)`

**src/lib/api/services/accounts.ts:**
- `getAccountsByUser(userId)`, `getAccountById(id)`, `createAccount(data)`, etc.

**src/lib/api/services/records.ts:**
- `getRecords(params)`, `createRecord(data)`, `updateRecord(id, data)`, etc.

And similar for categories, labels, and templates.

#### React Query Hooks:

**src/hooks/useUsers.ts:**
```typescript
export const useUsers = () => useQuery({
  queryKey: ['users'],
  queryFn: usersApi.getUsers
});

export const useCreateUser = () => useMutation({
  mutationFn: usersApi.createUser,
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ['users'] })
});
```

**Deliverable:** Complete type-safe API layer with React Query hooks

---

### Phase 3: UI Component Library Setup (Tasks 16-20)
**Goal:** Install and configure shadcn/ui components needed for the application

#### Components to Install:
```bash
# Base components
npx shadcn-ui@latest add button card input label select dialog dropdown-menu table

# Form components
npx shadcn-ui@latest add form checkbox radio-group textarea calendar

# Utility components
npx shadcn-ui@latest add toast alert badge separator tabs command skeleton
```

#### Custom Components to Build:

**CurrencyDisplay Component:**
- Format numbers as currency (default LKR)
- Support multiple currencies for future
- Color-code positive (green) vs negative (red)

**DateRangePicker Component:**
- Built on shadcn/ui Calendar
- Quick presets (This Month, Last Month, Last 3 Months, This Year)
- Custom range selection

**Deliverable:** Fully configured component library ready for use

---

### Phase 4: Authentication & User Context (Tasks 21-23)
**Goal:** Create user management and context (temporary until real auth is added)

#### User Context:
- Store current user ID and user object
- Provide user data throughout the app
- Handle user switching (temporary feature)

#### User Selector Component:
- Dropdown to select active user
- Display in top navigation
- Persist selection to localStorage

**Note:** This will be replaced with proper authentication (JWT, OAuth, etc.) in a future phase.

**Deliverable:** Working user context and selector for development

---

### Phase 5: Dashboard Overview (Tasks 24-27)
**Goal:** Create the main dashboard landing page with financial overview

#### Summary Cards:
1. **Total Balance** - Sum of all account balances
2. **This Month's Income** - Total income for current month
3. **This Month's Expenses** - Total expenses for current month
4. **Account Count** - Number of active accounts

#### Charts:
1. **Income vs Expenses Chart**
   - Bar or Line chart
   - Last 6 months comparison
   - Use Recharts BarChart/LineChart

2. **Spending by Category**
   - Pie or Donut chart
   - Current month expenses breakdown
   - Use Recharts PieChart

#### Recent Transactions:
- Table showing last 10 transactions
- Display: Date, Description, Category, Amount, Account
- Click to view/edit

**Route:** `/dashboard` or `/` (homepage)

**Deliverable:** Fully functional dashboard with live data from backend

---

### Phase 6: Accounts Management (Tasks 28-33)
**Goal:** Complete account management functionality

#### Accounts List Page (`/accounts`)
- Grid/List view of all accounts
- Display: Account name, type, balance
- Color-code account types
- Show credit/overdraft limits where applicable

#### Account Detail Page (`/accounts/[id]`)
- Account information card
- Transaction history for this account
- Balance trend chart
- Edit/Delete actions

#### Forms:
1. **Add Account Dialog**
   - Account name (required)
   - Account type selector (General/Credit/Overdraft)
   - Initial balance (required)
   - Credit limit (if Credit account)
   - Overdraft limit (if Overdraft account)
   - Validation with Zod schema

2. **Edit Account Dialog**
   - Pre-filled form
   - Can't change account type or initial balance
   - Can update name and limits

3. **Delete Account Confirmation**
   - Warning if account has transactions
   - Show impact on total balance

**Deliverable:** Complete account management with CRUD operations

---

### Phase 7: Transactions Management (Tasks 34-42)
**Goal:** Build comprehensive transaction tracking and management

#### Transactions List Page (`/transactions`)
- TanStack Table with sorting and filtering
- Columns: Date, Description, Category, Labels, Account, Amount, Type
- Pagination (if many records)
- Quick actions: Edit, Delete

#### Filters:
- Date range picker
- Account selector (multi-select)
- Category selector (multi-select)
- Record type filter (Income/Expense/Transfer)
- Label filter (multi-select)
- Search by description

#### Add/Edit Transaction Form:
1. **Record Type Selector** (Income/Expense/Transfer)
2. **Dynamic Fields Based on Type:**
   - **Income/Expense:** Account, Category, Amount, Date, Description, Labels
   - **Transfer:** From Account, To Account, Amount, Date, Description, Labels

3. **Category Selector:**
   - Filter categories by type (Income categories for Income records, etc.)
   - Display system defaults and user custom categories

4. **Label Multi-Selector:**
   - Multi-select with color display
   - Create new label inline (optional enhancement)

5. **Validation:**
   - Required fields: Type, Account, Category, Amount, Date
   - Amount must be positive
   - Transfer: From and To accounts must be different
   - Date can't be in the future (optional rule)

#### Transaction Detail Modal:
- Read-only view of all transaction data
- Display all labels with colors
- Show category and accounts
- Edit/Delete buttons

**Deliverable:** Full transaction management system with filtering

---

### Phase 8: Categories Management (Tasks 43-48)
**Goal:** Manage income and expense categories

#### Categories Page (`/categories`)
- Tabbed interface: "All", "Income", "Expense", "System Defaults", "My Categories"
- List view with category name and type
- Visual distinction for system vs user categories
- Edit/Delete only for user categories

#### Forms:
1. **Add Category Form**
   - Category name (required)
   - Category type (Income/Expense)
   - User ID (from context)

2. **Edit Category Form**
   - Only for user-created categories
   - Can change name but not type

3. **Delete Category**
   - Check if category is used in transactions
   - Show warning and usage count
   - Prevent deletion if in use (or ask to reassign)

#### Search:
- Real-time search by category name
- Backend endpoint: `/api/categories/user/{userId}/search?query=...`

**Deliverable:** Category management with system defaults support

---

### Phase 9: Labels Management (Tasks 49-53)
**Goal:** Create and manage color-coded labels

#### Labels Page (`/labels`)
- Grid or list view with color swatches
- Show usage statistics (used in X transactions)
- Tabs: "All Labels", "Used Labels", "Unused Labels"

#### Forms:
1. **Add Label Form**
   - Label name (required)
   - Color picker (HEX color)
   - Preview swatch

2. **Edit Label Form**
   - Update name and color
   - Show usage count

3. **Delete Label**
   - Show usage count
   - Confirm deletion
   - Labels are removed from transactions when deleted

#### Color Picker:
- Use a color picker library (e.g., `react-colorful`)
- Or predefined palette of colors
- Display color swatch in label badge

**Deliverable:** Complete label management with color support

---

### Phase 10: Templates Management (Tasks 54-59)
**Goal:** Create reusable transaction templates

#### Templates Page (`/templates`)
- List of all user templates
- Display: Template name, type, category, default amount
- Quick "Use Template" button
- Usage count statistics

#### Forms:
1. **Add Template Form**
   - Template name (required)
   - Record type (Income/Expense/Transfer)
   - Category
   - Default amount (optional)
   - Description (optional)

2. **Edit Template Form**
   - Update all fields

3. **Delete Template**
   - Simple confirmation

#### Use Template Feature:
- Modal/Dialog to create transaction from template
- Pre-fill all fields from template
- Allow overrides:
  - Amount (if different from default)
  - Date (default to today)
  - Account (required if not in template)
  - For transfers: To Account
- Backend endpoint: `POST /api/templates/{id}/use`

**Deliverable:** Template system for recurring transactions

---

### Phase 11: Utilities & Enhancements (Tasks 60-68)
**Goal:** Add utilities, error handling, and UX improvements

#### Utilities:
1. **Currency Formatting** (`lib/utils/currency.ts`)
   - Format numbers with LKR symbol and proper formatting
   - Support decimal places
   - Handle negative values

2. **Date Formatting** (`lib/utils/date.ts`)
   - Format dates for display
   - Parse ISO dates from backend
   - Date range utilities

3. **Transaction Calculations** (`lib/utils/calculations.ts`)
   - Calculate total income/expenses
   - Calculate net (income - expenses)
   - Account balance calculations

#### Error Handling:
- Global error boundary component
- API error handling with user-friendly messages
- Form validation error displays
- Network error handling with retry

#### Loading States:
- Skeleton loaders for all pages
- Loading spinners for forms
- Optimistic updates for better UX

#### Toast Notifications:
- Success messages (Record created, Account updated, etc.)
- Error messages (Failed to save, Validation errors, etc.)
- Use shadcn/ui Toast component

#### Responsive Design:
- Mobile-first approach
- Responsive navigation (hamburger menu on mobile)
- Responsive tables (stack on mobile or horizontal scroll)
- Responsive charts

#### Empty States:
- "No accounts yet" with CTA to create first account
- "No transactions" with CTA to add transaction
- "No categories" state
- "No labels" state
- "No templates" state

**Deliverable:** Polished UX with proper error handling and responsive design

---

### Phase 12: Testing & Documentation (Tasks 69-73)
**Goal:** Ensure code quality and provide documentation

#### Code Quality:
1. **ESLint Configuration**
   - Use Next.js recommended config
   - Add custom rules for project standards

2. **Prettier Configuration**
   - Consistent code formatting
   - Integrate with ESLint

#### Testing:
1. **Vitest Setup**
   - Configure for unit testing
   - Mock API calls

2. **Unit Tests**
   - Test utility functions (currency, date, calculations)
   - Test custom hooks (React Query hooks)

3. **Component Tests**
   - Test critical forms (Add Transaction, Add Account)
   - Test validation logic
   - Test user interactions

#### Documentation:
1. **README.md**
   - Project overview
   - Setup instructions
   - Available scripts
   - Environment variables
   - Folder structure

2. **API Integration Docs**
   - How to add new endpoints
   - Data flow diagrams
   - Type definitions guide

3. **.env.example**
   - All required environment variables
   - Example values
   - Comments explaining each variable

**Deliverable:** Well-tested, documented codebase

---

### Phase 13: Deployment Preparation (Tasks 74-76)
**Goal:** Prepare application for deployment

#### Docker Configuration:
```dockerfile
# Example Dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

#### Production Optimizations:
- Optimize images (use Next.js Image component)
- Code splitting (automatic with Next.js)
- Bundle analysis
- Performance monitoring

#### End-to-End Testing:
- Test complete user flows:
  1. Create user → Create account → Add transaction
  2. Create category → Use in transaction
  3. Create label → Tag transactions
  4. Create template → Use template → Create transaction

#### Performance Optimization:
- Analyze bundle size with `@next/bundle-analyzer`
- Lazy load heavy components
- Optimize React Query cache settings
- Use React Server Components where possible

**Deliverable:** Production-ready application

---

## Navigation Structure

```
┌─────────────────────────────────────┐
│         Top Navigation              │
│  [Logo] [User Selector] [Settings]  │
└─────────────────────────────────────┘
┌──────────┬──────────────────────────┐
│ Sidebar  │                          │
│          │                          │
│ Dashboard│      Main Content        │
│ Accounts │                          │
│ Trans.   │                          │
│ Categ.   │                          │
│ Labels   │                          │
│ Templates│                          │
│          │                          │
└──────────┴──────────────────────────┘
```

### Routes:
- `/` or `/dashboard` - Dashboard overview
- `/accounts` - Accounts list
- `/accounts/[id]` - Account details
- `/transactions` - Transactions list
- `/categories` - Categories management
- `/labels` - Labels management
- `/templates` - Templates management

---

## Key Features Summary

### Implemented Features:
1. Multi-user support (via user selector)
2. Account management (General, Credit, Overdraft)
3. Transaction tracking (Income, Expense, Transfer)
4. Category system (System defaults + User custom)
5. Color-coded labels
6. Reusable templates
7. Advanced filtering and search
8. Financial charts and analytics
9. Responsive design
10. Form validation
11. Error handling
12. Loading states

### Future Enhancements (Phase 2 - AI Integration):
1. Authentication system (JWT/OAuth)
2. AI chatbot interface
3. Natural language transaction entry
4. Financial insights and recommendations
5. MCP integration for AI agents
6. Predictive analytics
7. Budget planning with AI
8. Export to PDF/CSV
9. Multi-currency support
10. Recurring transactions

---

## Success Criteria

The Dashboard Application is considered complete when:

1. ✅ All CRUD operations work for Users, Accounts, Records, Categories, Labels, Templates
2. ✅ Dashboard displays accurate financial overview with charts
3. ✅ Filtering and search work correctly
4. ✅ Forms have proper validation and error handling
5. ✅ Application is responsive on mobile, tablet, desktop
6. ✅ Loading states and empty states are implemented
7. ✅ All API endpoints are integrated correctly
8. ✅ Type safety is maintained throughout (no TypeScript errors)
9. ✅ Core functionality is tested
10. ✅ Application is documented and ready for deployment

---

## Estimated Task Breakdown

- **Phase 1-2 (Foundation & API):** ~15 tasks
- **Phase 3-4 (UI & Context):** ~8 tasks
- **Phase 5 (Dashboard):** ~4 tasks
- **Phase 6 (Accounts):** ~6 tasks
- **Phase 7 (Transactions):** ~9 tasks
- **Phase 8 (Categories):** ~6 tasks
- **Phase 9 (Labels):** ~5 tasks
- **Phase 10 (Templates):** ~6 tasks
- **Phase 11 (Utilities):** ~9 tasks
- **Phase 12 (Testing):** ~5 tasks
- **Phase 13 (Deployment):** ~3 tasks

**Total:** 76 tasks

---

## Next Steps

1. Review this plan with the team
2. Set up the development environment
3. Begin Phase 1: Project Foundation
4. Follow the task list in order for systematic development
5. Track progress using the todo list
6. Iterate and adjust as needed during development

---

## Notes

- Backend API must be running at `http://localhost:8080` for development
- Default currency is LKR (Sri Lankan Rupee) as per backend configuration
- System default categories should be seeded in the backend database
- This plan assumes backend API is fully functional and tested
- User authentication will be added in a future enhancement phase
- MCP integration for AI will be Phase 2 after dashboard is complete

---

**Document Version:** 1.0
**Created:** 2026-01-18
**Last Updated:** 2026-01-18
