import api from './api';

export default {
    getAdminStats() {
        return api.get('/dashboard/admin');
    }
};
