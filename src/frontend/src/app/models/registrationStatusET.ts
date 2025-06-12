export enum RegistrationStatusET {
    REGISTERED,
    PENDING,
    BLOCKED
}

export const RegistrationStatuses: RegistrationStatusET[] = [
  RegistrationStatusET.REGISTERED,
  RegistrationStatusET.PENDING,
  RegistrationStatusET.BLOCKED
];

export const RegistrationStatusLabels: { [key in RegistrationStatusET]: string } = {
  [RegistrationStatusET.PENDING]: 'Pending',
  [RegistrationStatusET.REGISTERED]: 'Registered',
  [RegistrationStatusET.BLOCKED]: 'Blocked'
};