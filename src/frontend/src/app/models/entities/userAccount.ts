export interface UserAccount {
    id?: string;
    keycloakId: string;
    role: string;
    name?: string;
    surname?: string;
    username?: string;
    email?: string;
    token?: string;
    newPassword?: string;
    confirmPassword?: string;
}